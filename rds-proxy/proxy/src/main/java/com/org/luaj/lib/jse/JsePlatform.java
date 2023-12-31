/*******************************************************************************
 * Copyright (c) 2009-2011 Luaj.org. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 ******************************************************************************/
package com.org.luaj.lib.jse;

import com.org.luaj.compiler.LuaC;
import com.org.luaj.LuaTable;
import com.org.luaj.LuaThread;
import com.org.luaj.lib.*;

public class JsePlatform {

	/**
	 * Create a standard set of globals for JSE including all the libraries.
	 * 
	 * @return Table of globals initialized with the standard JSE libraries
	 * @see JsePlatform
	 */
	public static LuaTable standardGlobals() {
		LuaTable _G = new LuaTable();
		_G.load(new JseBaseLib());
		_G.load(new PackageLib());
		_G.load(new TableLib());
		_G.load(new StringLib());
		_G.load(new CoroutineLib());
		_G.load(new JseMathLib());
		_G.load(new JseIoLib());
		_G.load(new JseOsLib());
		_G.load(new StructLib());
		_G.load(new RedisLib());
		_G.load(new CJsonLib());
		_G.load(new LuajavaLib());
		LuaThread.setGlobals(_G);
		LuaC.install();
		return _G;		
	}
}
