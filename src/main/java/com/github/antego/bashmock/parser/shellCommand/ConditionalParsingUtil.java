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
import com.github.antego.bashmock.parser.BashPsiBuilder;
import com.github.antego.bashmock.parser.Parsing;
import com.intellij.psi.tree.TokenSet;

/**
 * @author jansorg
 */
public class ConditionalParsingUtil {
    private static TokenSet operators = TokenSet.create(BashTokenTypes.COND_OP, BashTokenTypes.COND_OP_EQ_EQ, BashTokenTypes.COND_OP_REGEX);
    private static TokenSet regExpEndTokens = TokenSet.create(BashTokenTypes.WHITESPACE, BashTokenTypes._BRACKET_KEYWORD);

    private ConditionalParsingUtil() {
    }

    public static boolean readTestExpression(BashPsiBuilder builder, TokenSet endTokens) {
        //fixme implement more intelligent test expression parsing

        boolean ok = true;

        while (ok && !endTokens.contains(builder.getTokenType())) {
            if (Parsing.word.isWordToken(builder)) {
                ok = Parsing.word.parseWord(builder);
            } else if (builder.getTokenType() == BashTokenTypes.COND_OP_NOT) {
                builder.advanceLexer();
                ok = readTestExpression(builder, endTokens);
            } else if (builder.getTokenType() == BashTokenTypes.COND_OP_REGEX) {
                builder.advanceLexer();

                //eat optional whitespace in front
                if (builder.getTokenType(true) == BashTokenTypes.WHITESPACE) {
                    //builder.advanceLexer();
                }

                //parse the regex
                ok = parseRegularExpression(builder);
            } else if (operators.contains(builder.getTokenType())) {
                builder.advanceLexer();
            } else {
                ok = false;
                break;
            }
        }

        return ok;
    }

    public static boolean parseRegularExpression(BashPsiBuilder builder) {
        int count = 0;

        //simple solution: read to the next whitespace, unless we are in [] brackets
        while (!builder.eof() && !regExpEndTokens.contains(builder.rawLookup(0))) {
            builder.advanceLexer();
            count++;
        }

        return count > 0;
    }
}
