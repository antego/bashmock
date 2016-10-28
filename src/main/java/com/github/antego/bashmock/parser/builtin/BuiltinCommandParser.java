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

import com.github.antego.bashmock.parser.ParsingChain;

/**
 * Comination of a list of internal command implementations.
 * <br>
 * @author jansorg
 */
public class BuiltinCommandParser extends ParsingChain {
    public BuiltinCommandParser() {
        addParsingFunction(new ExportCommand());
        addParsingFunction(new ReadonlyCommand());
        addParsingFunction(new DeclareCommand());
        addParsingFunction(new TypesetCommand());
        addParsingFunction(new ReadCommand());
        addParsingFunction(new LocalCommand());
        addParsingFunction(new IncludeCommand());
        addParsingFunction(new GetOptsCommand());
        addParsingFunction(new EvalCommandParsing());

        addParsingFunction(new LetCommand());
    }
}
