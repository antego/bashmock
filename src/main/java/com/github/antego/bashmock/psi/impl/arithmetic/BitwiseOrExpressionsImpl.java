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
import com.github.antego.bashmock.psi.api.arithmetic.BitwiseOr;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.IElementType;

/**
 * @author jansorg
 */
public class BitwiseOrExpressionsImpl extends AbstractExpression implements BitwiseOr {
    public BitwiseOrExpressionsImpl(final ASTNode astNode) {
        super(astNode, "BitwiseOrExpr", Type.TwoOperands);
    }

    @Override
    protected Long compute(long currentValue, IElementType operator, Long nextExpressionValue) {
        if (operator == BashTokenTypes.PIPE) {
            return currentValue | nextExpressionValue;
        }

        return null;
    }

}