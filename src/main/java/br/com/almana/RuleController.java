package br.com.almana;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.script.*;
import javax.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by francisco on 9/10/16.
 */
@RestController
public class RuleController {

  @RequestMapping(value = "/rules", method = RequestMethod.POST)
  public ResponseEntity<Void> addNewRule(@NotNull @RequestBody String script) {
    Scripts.add(script);
    return ResponseEntity.ok().build();
  }

  @RequestMapping(value = "/rules", method = RequestMethod.GET)
  public ResponseEntity<List<String>> listRules() {
    return ResponseEntity.ok(Scripts.getAll());
  }

  @RequestMapping(value = "/rules", method = RequestMethod.DELETE)
  public ResponseEntity<Void> clearRules() {
    Scripts.clear();
    return ResponseEntity.ok().build();
  }


  @RequestMapping(value = "/validate", method = RequestMethod.POST, consumes = "application/json")
  public int applyRules(@NotNull @RequestBody Purchase purchase) {

    final int[] score = {0};
    Scripts.getAll().forEach((script) -> {
      score[0] += evaluateExpression(script, purchase);
    });

    return score[0];
  }

  private static Integer evaluateExpression(String script,
                                            Purchase purchase) {
    ScriptEngineManager scriptManager = new ScriptEngineManager();
    ScriptEngine nashornEngine = scriptManager.getEngineByName("nashorn");
    try {
      putJavaVariableIntoEcmaScope(nashornEngine, purchase);
      nashornEngine.eval(script);
      Invocable invocable = (Invocable) nashornEngine;
      Number javaValue = (Number) invocable.invokeFunction("validate");
      return javaValue.intValue();
    } catch (ScriptException | NoSuchMethodException e) {
      e.printStackTrace();
      return 0;
    }
  }

  private static void putJavaVariableIntoEcmaScope(ScriptEngine engine,
                                                   Purchase purchase) {

    String purchaseJson = "{}";
    try {
      purchaseJson = new ObjectMapper().writeValueAsString(purchase);
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
    engine.put("purchase", purchaseJson);
  }

}


class Purchase {
  private Person person;
  private BigDecimal value;

  public BigDecimal getValue() {
    return value;
  }

  public Person getPerson() {
    return person;
  }

  public void setPerson(Person person) {
    this.person = person;
  }

  public void setValue(BigDecimal value) {
    this.value = value;
  }
}

class Person {
  private String name;
  private int age;
  private String zipCode;

  public String getName() {
    return name;
  }

  public int getAge() {
    return age;
  }

  public String getZipCode() {
    return zipCode;
  }

  public void setAge(int age) {
    this.age = age;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setZipCode(String zipCode) {
    this.zipCode = zipCode;
  }
}

class Scripts {
  static List<String> scripts = new ArrayList<>();

  static void add(String script) {
    scripts.add(script);
  }

  static List<String> getAll() {
    return scripts;
  }

  public static void clear() {
    scripts.clear();
  }
}