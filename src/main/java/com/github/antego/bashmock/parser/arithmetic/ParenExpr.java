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

/**
 * Parses an optional parantheses expression. If not found it delegates to another function.
 * <br>
 * @author jansorg
 */
class ParenExpr implements ArithmeticParsingFunction {
    public boolean isValid(BashPsiBuilder builder) {
        return builder.getTokenType() == LEFT_PAREN;
    }

    public boolean parse(BashPsiBuilder builder) {
        if (!isValid(builder)) {
            return false;
        }

        //the marker has to contain the opening parenthesis
        PsiBuilder.Marker marker = builder.mark();

        builder.advanceLexer();
        boolean ok = ArithmeticFactory.entryPoint().parse(builder) && ParserUtil.conditionalRead(builder, RIGHT_PAREN);

        if (ok) {
            marker.done(ARITH_PARENS_ELEMENT);
            return true;
        }

        marker.drop();
        return false;
    }
}
