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

package com.github.antego.bashmock.parser.command;

import com.github.antego.bashmock.parser.BashPsiBuilder;
import com.github.antego.bashmock.parser.Parsing;
import com.github.antego.bashmock.parser.ParsingFunction;

/**
 * This simply delegates the parsing to the shellcommands. This way internal shell commands
 * are marked as commands, too.
 * <br>
 * @author jansorg
 */
public class ShellCommandDelegator implements ParsingFunction {
    public boolean isValid(BashPsiBuilder builder) {
        return Parsing.shellCommand.isValid(builder);
    }

    public boolean parse(BashPsiBuilder builder) {
        final boolean ok = Parsing.shellCommand.parse(builder);
        //parse optional redirect list, if the shell command parsed

        //fixme is this still required
        return ok && (Parsing.redirection.parseList(builder, true, true) || !ok);
    }
}
