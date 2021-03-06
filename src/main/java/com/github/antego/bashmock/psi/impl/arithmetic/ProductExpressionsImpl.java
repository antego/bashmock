/*
 * Copyright (c) Joachim Ansorg, mail@ansorg-it.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.github.antego.bashmock.psi.impl.arithmetic;

import com.github.antego.bashmock.lexer.BashTokenTypes;
import com.github.antego.bashmock.psi.api.arithmetic.ArithmeticExpression;
import com.github.antego.bashmock.psi.api.arithmetic.ProductExpression;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.IElementType;

import java.util.List;

/**
 * @author jansorg
 */
public class ProductExpressionsImpl extends AbstractExpression implements ProductExpression {
    public ProductExpressionsImpl(final ASTNode astNode) {
        super(astNode, "ArithProductExpr", Type.TwoOperands);
    }

    @Override
    protected Long compute(long currentValue, IElementType operator, Long nextExpressionValue) {
        if (operator == BashTokenTypes.ARITH_MULT) {
            return currentValue * nextExpressionValue;
        } else if (operator == BashTokenTypes.ARITH_DIV) {
            return currentValue / nextExpressionValue;
        } else if (operator == BashTokenTypes.ARITH_MOD) {
            return currentValue % nextExpressionValue;
        }

        return null;
    }

    public boolean hasDivisionRemainder() {
        List<ArithmeticExpression> subs = subexpressions();

        if (subs.size() == 2 && findOperator() == BashTokenTypes.ARITH_DIV) {
            if (!subs.get(0).isStatic() || !subs.get(1).isStatic()) {
                return false;
            }

            long leftValue = subs.get(0).computeNumericValue();
            long rightValue = subs.get(1).computeNumericValue();

            return leftValue != ((leftValue / rightValue) * rightValue);
        }

        return false;
    }
}