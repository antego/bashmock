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

package com.github.antego.bashmock.parser.variable;

import com.github.antego.bashmock.parser.BashPsiBuilder;
import com.github.antego.bashmock.parser.Parsing;
import com.github.antego.bashmock.parser.ParsingFunction;
import com.github.antego.bashmock.parser.util.ParserUtil;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.TokenSet;

/**
 * Parses a combined variable. A combined variable can be a subshell expression, a
 * parameter expansion or an arithmetic subexpression.
 * <br>
 *
 * @author jansorg
 */
public class ComposedVariableParsing implements ParsingFunction {
    private static final TokenSet acceptedStarts = TokenSet.create(
            LEFT_CURLY, LEFT_PAREN, EXPR_ARITH, EXPR_CONDITIONAL, EXPR_ARITH_SQUARE
    );

    public boolean isValid(BashPsiBuilder builder) {
        IElementType first = builder.rawLookup(0);
        IElementType second = builder.rawLookup(1);

        return first == DOLLAR && acceptedStarts.contains(second);
    }

    public boolean parse(BashPsiBuilder builder) {
        final PsiBuilder.Marker varMarker = builder.mark();
        builder.advanceLexer(); //DOLLAR token

        final IElementType nextToken = builder.getTokenType(true);
        if (nextToken == WHITESPACE) {
            varMarker.drop();
            return false;
        }

        //check if a subshell of command group is following
        boolean ok;
        if (Parsing.parameterExpansionParsing.isValid(builder)) {
            ok = Parsing.parameterExpansionParsing.parse(builder);
        } else if (Parsing.shellCommand.arithmeticParser.isValid(builder)) {
            ok = Parsing.shellCommand.arithmeticParser.parse(builder);
        } else if (Parsing.shellCommand.subshellParser.isValid(builder)) {
            ok = Parsing.shellCommand.subshellParser.parse(builder);
        } else {
            ParserUtil.error(varMarker, "parser.unexpected.token");
            return false;
        }

        if (ok) {
            varMarker.done(VAR_COMPOSED_VAR_ELEMENT);
        } else {
            varMarker.drop();
        }

        return true;
    }
}
