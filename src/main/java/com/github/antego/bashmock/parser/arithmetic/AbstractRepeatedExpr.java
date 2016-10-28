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

package com.github.antego.bashmock.parser.arithmetic;

import com.github.antego.bashmock.parser.BashPsiBuilder;
import com.github.antego.bashmock.parser.util.ParserUtil;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

/**
 * Abstract class for a repeated expression which delegates the parsing of the subexpressions to
 * another parsing function.
 * <br>
 * @author jansorg
 */
class AbstractRepeatedExpr implements ArithmeticParsingFunction {
    private final ArithmeticParsingFunction expressionParser;
    private final TokenSet operators;
    private boolean prefixOperator;
    private final IElementType partMarker;
    private int maxRepeats;
    private final String debugInfo;
    private final boolean checkWhitespace;

    /**
     * @param expressionParser
     * @param operators
     * @param prefixOperator   true if the operators are expected to be before the subexpression, otherweise between subexpressions
     * @param partMarker
     * @param maxRepeats
     * @param debugInfo
     */
    AbstractRepeatedExpr(ArithmeticParsingFunction expressionParser, TokenSet operators, boolean prefixOperator, IElementType partMarker, int maxRepeats, String debugInfo) {
        this.expressionParser = expressionParser;
        this.operators = operators;
        this.prefixOperator = prefixOperator;
        this.partMarker = partMarker;
        this.maxRepeats = maxRepeats;
        this.debugInfo = debugInfo;

        this.checkWhitespace = operators.contains(WHITESPACE);
    }

    public boolean isValid(BashPsiBuilder builder) {
        if (prefixOperator && operators.contains(builder.getTokenType(checkWhitespace))) {
            return true;
        } else if (expressionParser.isValid(builder)) {
            return true;
        }

        return isValidParentesisExpression(builder);
    }

    private boolean isValidParentesisExpression(BashPsiBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();

        ArithmeticParsingFunction parenthesisParser = ArithmeticFactory.parenthesisParser();
        boolean ok = parenthesisParser.isValid(builder)
                && parenthesisParser.parse(builder)
                && operators.contains(builder.getTokenType(checkWhitespace));

        marker.rollbackTo();
        return ok;
    }

    public boolean parse(BashPsiBuilder builder) {
        return prefixOperator
                ? parseWithPrefixOperator(builder)
                : parseWithNonPrefixOperator(builder);
    }

    private boolean parseWithPrefixOperator(BashPsiBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();

        int count = 0;
        boolean ok;

        do {
            ok = ParserUtil.conditionalRead(builder, operators);
            count++;
        } while (ok && (maxRepeats <= 0 || count < maxRepeats));

        if (expressionParser.isValid(builder)) {
            ok = expressionParser.parse(builder);
        } else {
            ok = ArithmeticFactory.parenthesisParser().parse(builder);
        }

        if (ok && count > 1 && partMarker != null) {
            marker.done(partMarker);
        } else {
            marker.drop();
        }

        return ok;
    }

    private boolean parseWithNonPrefixOperator(BashPsiBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();

        int count = 0;
        boolean ok;

        do {
            if (expressionParser.isValid(builder)) {
                ok = expressionParser.parse(builder);
            } else {
                ok = ArithmeticFactory.parenthesisParser().parse(builder);
            }

            count++;
        } while (ok && (maxRepeats <= 0 || count < maxRepeats) && ParserUtil.conditionalRead(builder, operators));

        if (ok && count > 1 && partMarker != null) {
            marker.done(partMarker);
        } else {
            marker.drop();
        }

        return ok;
    }

    @Override
    public String toString() {
        return "RepeatedExpr:" + debugInfo + ": " + super.toString();
    }
}