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

package com.github.antego.bashmock.parser.shellCommand;

import com.github.antego.bashmock.lexer.BashTokenTypes;
import com.github.antego.bashmock.parser.BashElementTypes;
import com.github.antego.bashmock.parser.BashPsiBuilder;
import com.github.antego.bashmock.parser.ParsingFunction;
import com.github.antego.bashmock.parser.arithmetic.ArithmeticFactory;
import com.github.antego.bashmock.parser.util.ParserUtil;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;

/**
 * Parsing function for arithmetic expressions.
 * It delegates to the actual arithmetic expression parser implementation but takes
 * care of start and end marker tokens.
 * <br>
 * @author jansorg
 */
public final class ArithmeticParser implements ParsingFunction {
    private static final ParsingFunction arithmeticExprParser = ArithmeticFactory.entryPoint();

    public boolean isValid(BashPsiBuilder builder) {
        IElementType tokenType = builder.getTokenType();
        return tokenType == BashTokenTypes.EXPR_ARITH || tokenType == BashTokenTypes.EXPR_ARITH_SQUARE;
    }

    /**
     * Parses a default arithmetic expression, e.g (( a+3 ))
     *
     * @param builder The builder to use
     * @return Whether the operation has been successful
     */
    public boolean parse(BashPsiBuilder builder) {
        //special handling for empty expressions
        if (ParserUtil.hasNextTokens(builder, true, EXPR_ARITH, _EXPR_ARITH)) {
            builder.advanceLexer();
            builder.advanceLexer();
            return true;
        }

        //special handling for empty expressions
        if (ParserUtil.hasNextTokens(builder, true, EXPR_ARITH_SQUARE, _EXPR_ARITH_SQUARE)) {
            builder.advanceLexer();
            builder.advanceLexer();
            return true;
        }

        if (builder.getTokenType() == BashTokenTypes.EXPR_ARITH_SQUARE) {
            return parse(builder, BashTokenTypes.EXPR_ARITH_SQUARE, BashTokenTypes._EXPR_ARITH_SQUARE);
        }

        return parse(builder, BashTokenTypes.EXPR_ARITH, BashTokenTypes._EXPR_ARITH);
    }

    /**
     * Parses an arithmetic expression with specific start and end token. This
     * is useful for places where a different syntax as (()) is used, e.g. is
     * array assignment lists.
     *
     * @param builder    The builder to use
     * @param startToken The expected start token
     * @param endToken   The expected end token
     * @return The result
     */
    public boolean parse(BashPsiBuilder builder, IElementType startToken, IElementType endToken) {
        /*
            arith_command:	ARITH_CMD
         */
        if (builder.getTokenType() != startToken) {
            return false;
        }

        final PsiBuilder.Marker arithmetic = builder.mark();
        builder.advanceLexer(); //after the start token

        if (!arithmeticExprParser.parse(builder)) {
            builder.getTokenType();
            arithmetic.drop();
            ParserUtil.error(builder, "parser.unexpected.token");
            return false;
        }

        final IElementType lastToken = ParserUtil.getTokenAndAdvance(builder);
        if (lastToken != endToken) {
            arithmetic.drop();
            ParserUtil.error(builder, "parser.unexpected.token");
            return false;
        }

        arithmetic.done(BashElementTypes.ARITHMETIC_COMMAND);
        return true;
    }
}
