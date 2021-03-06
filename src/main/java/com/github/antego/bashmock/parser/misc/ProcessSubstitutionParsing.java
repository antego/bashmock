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

package com.github.antego.bashmock.parser.misc;

import com.github.antego.bashmock.parser.BashPsiBuilder;
import com.github.antego.bashmock.parser.Parsing;
import com.github.antego.bashmock.parser.ParsingFunction;
import com.github.antego.bashmock.parser.util.ParserUtil;
import com.intellij.lang.PsiBuilder;
import com.intellij.psi.tree.IElementType;

/**
 * Parses process substitution expressions, i.e.
 * <br>
 * <(bash subshell expression)
 * <br>
 * >(bash susbshell expression)
 * <br>
 * @author jansorg
 * 3:12
 */
public class ProcessSubstitutionParsing implements ParsingFunction {
    public boolean isValid(BashPsiBuilder builder) {
        IElementType first = builder.rawLookup(0);
        IElementType second = builder.rawLookup(1);

        return (first == LESS_THAN || first == GREATER_THAN) && second == LEFT_PAREN;
    }

    public boolean parse(BashPsiBuilder builder) {
        PsiBuilder.Marker marker = builder.mark();

        builder.getTokenType();
        builder.advanceLexer(); // first token

        IElementType second = builder.getTokenType(true);
        builder.advanceLexer(); //second token (i.e. LEFT_PAREN)

        if (second != LEFT_PAREN) {
            marker.drop();
            return false;
        }

        boolean ok = Parsing.list.parseCompoundList(builder, true, false);

        if (!ok) {
            marker.drop();
            //fixme error message ?
            return false;
        }

        //eat the closing parenthesis
        if (!ParserUtil.conditionalRead(builder, RIGHT_PAREN)) {
            marker.drop();
            return false;
        }

        marker.done(PROCESS_SUBSTITUTION_ELEMENT);
        return true;
    }
}
