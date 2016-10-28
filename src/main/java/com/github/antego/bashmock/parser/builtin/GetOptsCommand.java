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

package com.github.antego.bashmock.parser.builtin;

import com.github.antego.bashmock.parser.BashPsiBuilder;
import com.github.antego.bashmock.parser.Parsing;
import com.github.antego.bashmock.parser.ParsingFunction;
import com.github.antego.bashmock.parser.command.CommandParsingUtil;
import com.github.antego.bashmock.parser.util.ParserUtil;
import com.intellij.lang.PsiBuilder;

public class GetOptsCommand implements ParsingFunction {
    @Override
    public boolean isValid(BashPsiBuilder builder) {
        return "getopts".equals(builder.getTokenText());
    }

    @Override
    public boolean parse(BashPsiBuilder builder) {
        if (!isValid(builder)) {
            return false;
        }

        PsiBuilder.Marker cmdMarker = builder.mark();

        //eat the getopts and mark it
        PsiBuilder.Marker getOpts = builder.mark();
        builder.advanceLexer();
        getOpts.done(GENERIC_COMMAND_ELEMENT);

        //the first option is the option definition
        if (Parsing.word.isComposedString(builder.getTokenType())) {
            if (!Parsing.word.parseComposedString(builder)) {
                cmdMarker.drop();
                return false;
            }
        } else if (ParserUtil.isWordToken(builder.getTokenType())) {
            builder.advanceLexer();
        } else {
            cmdMarker.drop();
            builder.error("Expected the getopts option string");
            return false;
        }

        //the second argument is the variable name, i.e. the defined variable
        boolean varDefRead = CommandParsingUtil.readAssignment(builder, CommandParsingUtil.Mode.SimpleMode, true, false);
        if (!varDefRead) {
            cmdMarker.drop();
            builder.error("Expected getops variable name");
            return false;
        }

        //now read all remaining arguments in the command, these are the arguments parsed by getopts
        if (Parsing.word.isWordToken(builder)) {
            if (!Parsing.word.parseWordList(builder, false, false)) {
                cmdMarker.drop();
                return false;
            }
        }

        cmdMarker.done(SIMPLE_COMMAND_ELEMENT);

        return true;
    }
}
