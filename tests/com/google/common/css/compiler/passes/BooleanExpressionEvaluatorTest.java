/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.common.css.compiler.passes;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.ImmutableSet;
import com.google.common.css.compiler.ast.CssBooleanExpressionNode;
import com.google.common.css.compiler.ast.CssBooleanExpressionNode.Type;
import com.google.common.css.testing.UtilityTestCase;
import java.util.HashSet;
import java.util.Set;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for {@link BooleanExpressionEvaluator}.
 *
 */
@RunWith(JUnit4.class)
public class BooleanExpressionEvaluatorTest extends UtilityTestCase {
  @Test
  public void testOrWithTrueValues() {
    CssBooleanExpressionNode input = new CssBooleanExpressionNode(Type.OR, "",
        new CssBooleanExpressionNode(Type.CONSTANT, "a"),
        new CssBooleanExpressionNode(Type.CONSTANT, "b"), null);

    Set<String> truths = ImmutableSet.of("a");

    BooleanExpressionEvaluator eval =
        new BooleanExpressionEvaluator(input, truths);
    CssBooleanExpressionNode output = eval.evaluate();

    assertThat(output.getType()).isEqualTo(Type.CONSTANT);
    assertThat(output.getValue()).isEqualTo("TRUE");
  }

  @Test
  public void testNotWithTrueValue() {
    CssBooleanExpressionNode input = new CssBooleanExpressionNode(Type.NOT, "",
        new CssBooleanExpressionNode(Type.CONSTANT, "a"), null, null);

    Set<String> truths = ImmutableSet.of("a");

    BooleanExpressionEvaluator eval =
        new BooleanExpressionEvaluator(input, truths);
    CssBooleanExpressionNode output = eval.evaluate();

    assertThat(output.getType()).isEqualTo(Type.CONSTANT);
    assertThat(output.getValue()).isEqualTo("FALSE");
  }

  @Test
  public void testNotWithUnknown() {
    CssBooleanExpressionNode input = new CssBooleanExpressionNode(Type.NOT, "",
        new CssBooleanExpressionNode(Type.CONSTANT, "a"), null, null);

    Set<String> truths = new HashSet<String>();
    Set<String> falses = new HashSet<String>();

    BooleanExpressionEvaluator eval =
        new BooleanExpressionEvaluator(input, truths, falses);
    CssBooleanExpressionNode output = eval.evaluate();

    assertThat(output.getType()).isEqualTo(Type.NOT);
    assertThat(output.getValue()).isEmpty();
  }

  @Test
  public void testWithSimpleTrue() {
    CssBooleanExpressionNode input =
        new CssBooleanExpressionNode(Type.CONSTANT, "TRUE");

    Set<String> truths = new HashSet<String>();

    BooleanExpressionEvaluator eval =
        new BooleanExpressionEvaluator(input, truths);
    CssBooleanExpressionNode output = eval.evaluate();

    assertThat(output.getType()).isEqualTo(Type.CONSTANT);
    assertThat(output.getValue()).isEqualTo("TRUE");
  }

  @Test
  public void testAndWithTrueValues() {
    CssBooleanExpressionNode input = new CssBooleanExpressionNode(Type.AND, "",
        new CssBooleanExpressionNode(Type.CONSTANT, "a"),
        new CssBooleanExpressionNode(Type.CONSTANT, "b"), null);

    Set<String> truths = ImmutableSet.of("a", "b");

    BooleanExpressionEvaluator eval =
        new BooleanExpressionEvaluator(input, truths);

    CssBooleanExpressionNode output = eval.evaluate();

    assertThat(output.getType()).isEqualTo(Type.CONSTANT);
    assertThat(output.getValue()).isEqualTo("TRUE");
  }

  @Test
  public void testAndWithOneUnknown() {
    CssBooleanExpressionNode input = new CssBooleanExpressionNode(Type.AND, "",
        new CssBooleanExpressionNode(Type.CONSTANT, "a"),
        new CssBooleanExpressionNode(Type.CONSTANT, "b"), null);

    Set<String> truths = ImmutableSet.of("a");

    BooleanExpressionEvaluator eval =
        new BooleanExpressionEvaluator(input, truths);

    CssBooleanExpressionNode output = eval.evaluate();

    assertThat(output.getType()).isEqualTo(Type.CONSTANT);
    assertThat(output.getValue()).isEqualTo("FALSE");
  }

  @Test
  public void testAndWithTwoUnknowns() {
    CssBooleanExpressionNode input = new CssBooleanExpressionNode(Type.AND, "",
        new CssBooleanExpressionNode(Type.CONSTANT, "a"),
        new CssBooleanExpressionNode(Type.CONSTANT, "b"), null);

    Set<String> truths = new HashSet<String>();

    BooleanExpressionEvaluator eval =
        new BooleanExpressionEvaluator(input, truths);

    CssBooleanExpressionNode output = eval.evaluate();

    assertThat(output.getType()).isEqualTo(Type.CONSTANT);
    assertThat(output.getValue()).isEqualTo("FALSE");
  }

  @Test
  public void testNested1() {
    CssBooleanExpressionNode side1 = new CssBooleanExpressionNode(Type.AND, "",
        new CssBooleanExpressionNode(Type.CONSTANT, "a"),
        new CssBooleanExpressionNode(Type.CONSTANT, "b"), null);
    CssBooleanExpressionNode side2 = new CssBooleanExpressionNode(Type.AND, "",
        new CssBooleanExpressionNode(Type.CONSTANT, "c"),
        new CssBooleanExpressionNode(Type.CONSTANT, "FALSE"), null);
    CssBooleanExpressionNode input = new CssBooleanExpressionNode(Type.OR, "",
        side1,  side2, null);

    Set<String> truths = ImmutableSet.of("a");
    Set<String> falses = new HashSet<String>();

    BooleanExpressionEvaluator eval =
        new BooleanExpressionEvaluator(input, truths, falses);
    CssBooleanExpressionNode output = eval.evaluate();

    assertThat(output.getType()).isEqualTo(Type.CONSTANT);
    assertThat(output.getValue()).isEqualTo("b");
  }

  @Test
  public void testNested2() {
    CssBooleanExpressionNode side1 = new CssBooleanExpressionNode(Type.OR, "",
        new CssBooleanExpressionNode(Type.CONSTANT, "a"),
        new CssBooleanExpressionNode(Type.CONSTANT, "b"), null);
    CssBooleanExpressionNode side2 = new CssBooleanExpressionNode(Type.AND, "",
        new CssBooleanExpressionNode(Type.CONSTANT, "c"),
        new CssBooleanExpressionNode(Type.CONSTANT, "FALSE"), null);
    CssBooleanExpressionNode input = new CssBooleanExpressionNode(Type.OR, "",
        side1,  side2, null);

    Set<String> truths = ImmutableSet.of("b");
    Set<String> falses = new HashSet<String>();

    BooleanExpressionEvaluator eval =
        new BooleanExpressionEvaluator(input, truths, falses);
    CssBooleanExpressionNode output = eval.evaluate();

    assertThat(output.getType()).isEqualTo(Type.CONSTANT);
    assertThat(output.getValue()).isEqualTo("TRUE");
  }

  @Test
  public void testNested3() {
    CssBooleanExpressionNode side1 = new CssBooleanExpressionNode(Type.AND, "",
        new CssBooleanExpressionNode(Type.CONSTANT, "a"),
        new CssBooleanExpressionNode(Type.CONSTANT, "b"), null);
    CssBooleanExpressionNode side2 = new CssBooleanExpressionNode(Type.AND, "",
        new CssBooleanExpressionNode(Type.CONSTANT, "c"),
        new CssBooleanExpressionNode(Type.CONSTANT, "FALSE"), null);
    CssBooleanExpressionNode input = new CssBooleanExpressionNode(Type.OR, "",
        side1,  side2, null);

    Set<String> truths = ImmutableSet.of("b");
    Set<String> falses = new HashSet<String>();

    BooleanExpressionEvaluator eval =
        new BooleanExpressionEvaluator(input, truths, falses);
    CssBooleanExpressionNode output = eval.evaluate();

    assertThat(output.getType()).isEqualTo(Type.CONSTANT);
    assertThat(output.getValue()).isEqualTo("a");
  }

  @Test
  public void testNested4() {
    CssBooleanExpressionNode side1 = new CssBooleanExpressionNode(Type.OR, "",
        new CssBooleanExpressionNode(Type.CONSTANT, "a"),
        new CssBooleanExpressionNode(Type.CONSTANT, "b"), null);
    CssBooleanExpressionNode side2 = new CssBooleanExpressionNode(Type.OR, "",
        new CssBooleanExpressionNode(Type.CONSTANT, "c"),
        new CssBooleanExpressionNode(Type.CONSTANT, "d"), null);
    CssBooleanExpressionNode input = new CssBooleanExpressionNode(Type.OR, "",
        side1,  side2, null);

    Set<String> truths = new HashSet<String>();
    Set<String> falses = new HashSet<String>();

    BooleanExpressionEvaluator eval =
        new BooleanExpressionEvaluator(input, truths, falses);
    CssBooleanExpressionNode output = eval.evaluate();

    assertThat(output.getType()).isEqualTo(Type.OR);
    assertThat(output.getValue()).isEmpty();
  }
}
