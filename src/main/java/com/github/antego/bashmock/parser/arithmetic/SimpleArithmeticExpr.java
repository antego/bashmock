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

import com.github.antego.bashmock.lexer.BashTokenTypes;
import com.github.antego.bashmock.parser.BashPsiBuilder;
import com.github.antego.bashmock.parser.Parsing;
import com.github.antego.bashmock.parser.misc.ShellCommandParsing;
import com.github.antego.bashmock.parser.util.ParserUtil;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;

/**
 * Parsing of a simple arithmetic expressions.
 * <br>
 * @author jansorg
 */
class SimpleArithmeticExpr implements ArithmeticParsingFunction {
    public boolean isValid(BashPsiBuilder builder) {
        IElementType tokenType = builder.getTokenType();
        return tokenType == WORD || tokenType == ASSIGNMENT_WORD
                || arithLiterals.contains(tokenType)
                || arithmeticAdditionOps.contains(builder.getTokenType())
                || Parsing.var.isValid(builder)
                || Parsing.word.isWordToken(builder)
                || Parsing.word.isComposedString(tokenType)
                || ShellCommandParsing.arithmeticParser.isValid(builder);
    }

    public boolean parse(BashPsiBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();
        boolean ok;

        if (arithmeticAdditionOps.contains(builder.getTokenType())) {
            builder.advanceLexer(); //eat the prefix - or + token
            ok = parse(builder);
        } else if (Parsing.var.isValid(builder)) {
            ok = Parsing.var.parse(builder);
        } else if (builder.getTokenType() == BashTokenTypes.ARITH_NUMBER && builder.rawLookup(1) == BashTokenTypes.ARITH_BASE_CHAR) {
            //arithmetic base expression
            ParserUtil.getTokenAndAdvance(builder, false);
            ParserUtil.getTokenAndAdvance(builder, true);

            int startOffset = builder.getCurrentOffset();
            ok = true;
            do {
                IElementType nextToken = builder.getTokenType(true);
                if (nextToken == BashTokenTypes.ARITH_NUMBER) {
                    builder.advanceLexer();
                } else if (Parsing.word.isWordToken(builder)) {
                    ok = Parsing.word.parseWord(builder);
                    if (!ok) {
                        break;
                    }
                } else if (Parsing.var.isValid(builder)) {
                    ok = Parsing.var.parse(builder);
                    if (!ok) {
                        break;
                    }
                } else {
                    break;
                }
            } while (true);

            ok = ok && builder.getCurrentOffset() > startOffset;
        } else {
            //these are valid: 12, a. 12$a , 12${a}56, 12#10
            IElementType tokenType = builder.getTokenType(); //without whitespace
            do {
                if (tokenType == WORD) {
                    //mark "a" as a variable and not as a regular word token
                    ParserUtil.markTokenAndAdvance(builder, VAR_ELEMENT);
                    ok = true;
                } else if (tokenType == ASSIGNMENT_WORD) {
                    //mark "a[...]" as a variable and not as a regular word token
                    ParserUtil.markTokenAndAdvance(builder, VAR_ELEMENT);
                    ok = ShellCommandParsing.arithmeticParser.parse(builder, LEFT_SQUARE, RIGHT_SQUARE);
                } else if (arithLiterals.contains(tokenType)) {
                    builder.advanceLexer();
                    ok = true;
                } else if (Parsing.var.isValid(builder)) {
                    //fixme parse with whitespace on?
                    ok = Parsing.var.parse(builder);
                } else if (Parsing.word.isComposedString(tokenType)) {
                    ok = Parsing.word.parseComposedString(builder);
                } else if (Parsing.word.isWordToken(builder)) {
                    ok = Parsing.word.parseWord(builder);
                } else if (ShellCommandParsing.arithmeticParser.isValid(builder)) {
                    ok = ShellCommandParsing.arithmeticParser.parse(builder);
                } else {
                    ok = false;
                    break;
                }

                //next, including whitespace
                tokenType = builder.getTokenType(true);
            } while (ok && isValidPart(builder, tokenType));

            //FIXME checking twice in the loop and in the invariant condition is not efficient
        }

        if (ok) {
            marker.done(ARITH_SIMPLE_ELEMENT);
        } else {
            marker.drop();
        }

        return ok;
    }

    private boolean isValidPart(BashPsiBuilder builder, IElementType tokenType) {
        return tokenType == WORD
                || arithLiterals.contains(tokenType)
                || Parsing.var.isValid(builder)
                || Parsing.word.isWordToken(builder)
                || Parsing.word.isComposedString(tokenType)
                || ShellCommandParsing.arithmeticParser.isValid(builder);
    }
}
