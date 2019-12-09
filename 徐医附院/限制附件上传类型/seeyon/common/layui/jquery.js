/* prevent execution of jQuery if included more than once */
if(typeof window.jQuery == "undefined") {
/*
 * jQuery  - New Wave Javascript
 *
 * Copyright (c)  John Resig (jquery.com)
 * Dual licensed under the MIT (MIT-LICENSE.txt)
 * and GPL (GPL-LICENSE.txt) licenses.
 *
 * $Date: 2012-09-24 16:25:00 +0800 (Mon, 24 Sep 2012) $
 * $Rev: 1222 $
 */

// Global undefined variable
window.undefined = window.undefined;
var jQuery = function(a,c) {
	// If the context is global, return a new object
	if ( window == this )
		return new jQuery(a,c);

	// Make sure that a selection was provided
	a = a || document;
	
	// HANDLE: $(function)
	// Shortcut for document ready
	if ( jQuery.isFunction(a) )
		return new jQuery(document)[ jQuery.fn.ready ? "ready" : "load" ]( a );
	
	// Handle HTML strings
	if ( typeof a  == "string" ) {
		// HANDLE: $(html) -> $(array)
		var m = /^[^<]*(<(.|\n)+>)[^>]*$/.exec(a);
		if ( m )
			a = jQuery.clean( [ m[1] ] );
		
		// HANDLE: $(expr)
		else
			return new jQuery( c ).find( a );
	}
	
	return this.setArray(
		// HANDLE: $(array)
		a.constructor == Array && a ||

		// HANDLE: $(arraylike)
		// Watch for when an array-like object is passed as the selector
		(a.jquery || a.length && a != window && !a.nodeType && a[0] != undefined && a[0].nodeType) && jQuery.makeArray( a ) ||

		// HANDLE: $(*)
		[ a ] );
};

// Map over the $ in case of overwrite
if ( typeof $ != "undefined" )
	jQuery._$ = $;
	
// Map the jQuery namespace to the '$' one
var $ = jQuery;

jQuery.fn = jQuery.prototype = {
	jquery: "1.1.1",

	size: function() {
		return this.length;
	},
	
	length: 0,

	get: function( num ) {
		return num == undefined ?

			// Return a 'clean' array
			jQuery.makeArray( this ) :

			// Return just the object
			this[num];
	},
	pushStack: function( a ) {
		var ret = jQuery(this);
		ret.prevObject = this;
		return ret.setArray( a );
	},
	setArray: function( a ) {
		this.length = 0;
		[].push.apply( this, a );
		return this;
	},
	each: function( fn, args ) {
		return jQuery.each( this, fn, args );
	},
	index: function( obj ) {
		var pos = -1;
		this.each(function(i){
			if ( this == obj ) pos = i;
		});
		return pos;
	},

	attr: function( key, value, type ) {
		var obj = key;
		
		// Look for the case where we're accessing a style value
		if ( key.constructor == String )
			if ( value == undefined )
				return this.length && jQuery[ type || "attr" ]( this[0], key ) || undefined;
			else {
				obj = {};
				obj[ key ] = value;
			}
		
		// Check to see if we're setting style values
		return this.each(function(index){
			// Set all the styles
			for ( var prop in obj )
				jQuery.attr(
					type ? this.style : this,
					prop, jQuery.prop(this, obj[prop], type, index, prop)
				);
		});
	},

	css: function( key, value ) {
		return this.attr( key, value, "curCSS" );
	},

	text: function(e) {
		if ( typeof e == "string" )
			return this.empty().append( document.createTextNode( e ) );

		var t = "";
		jQuery.each( e || this, function(){
			jQuery.each( this.childNodes, function(){
				if ( this.nodeType != 8 )
					t += this.nodeType != 1 ?
						this.nodeValue : jQuery.fn.text([ this ]);
			});
		});
		return t;
	},

	wrap: function() {
		// The elements to wrap the target around
		var a = jQuery.clean(arguments);

		// Wrap each of the matched elements individually
		return this.each(function(){
			// Clone the structure that we're using to wrap
			var b = a[0].cloneNode(true);

			// Insert it before the element to be wrapped
			this.parentNode.insertBefore( b, this );

			// Find the deepest point in the wrap structure
			while ( b.firstChild )
				b = b.firstChild;

			// Move the matched element to within the wrap structure
			b.appendChild( this );
		});
	},
	append: function() {
		return this.domManip(arguments, true, 1, function(a){
			this.appendChild( a );
		});
	},
	prepend: function() {
		return this.domManip(arguments, true, -1, function(a){
			this.insertBefore( a, this.firstChild );
		});
	},
	before: function() {
		return this.domManip(arguments, false, 1, function(a){
			this.parentNode.insertBefore( a, this );
		});
	},
	after: function() {
		return this.domManip(arguments, false, -1, function(a){
			this.parentNode.insertBefore( a, this.nextSibling );
		});
	},
	end: function() {
		return this.prevObject || jQuery([]);
	},
	find: function(t) {
		return this.pushStack( jQuery.map( this, function(a){
			return jQuery.find(t,a);
		}) );
	},
	clone: function(deep) {
		return this.pushStack( jQuery.map( this, function(a){
			return a.cloneNode( deep != undefined ? deep : true );
		}) );
	},

	filter: function(t) {
		return this.pushStack(
			jQuery.isFunction( t ) &&
			jQuery.grep(this, function(el, index){
				return t.apply(el, [index])
			}) ||

			jQuery.multiFilter(t,this) );
	},

	not: function(t) {
		return this.pushStack(
			t.constructor == String &&
			jQuery.multiFilter(t,this,true) ||

			jQuery.grep(this,function(a){
					if ( t.constructor == Array || t.jquery )
						return jQuery.inArray( t, a ) < 0;
					else
						return a != t;
			}) );
	},

	add: function(t) {
		return this.pushStack( jQuery.merge(
			this.get(),
			t.constructor == String ?
				jQuery(t).get() :
				t.length != undefined && !t.nodeName ?
					t : [t] )
		);
	},
	is: function(expr) {
		return expr ? jQuery.filter(expr,this).r.length > 0 : false;
	},

	val: function( val ) {
		return val == undefined ?
			( this.length ? this[0].value : null ) :
			this.attr( "value", val );
	},

	html: function( val ) {
		return val == undefined ?
			( this.length ? this[0].innerHTML : null ) :
			this.empty().append( val );
	},
	domManip: function(args, table, dir, fn){
		var clone = this.length > 1; 
		var a = jQuery.clean(args);
		if ( dir < 0 )
			a.reverse();

		return this.each(function(){
			var obj = this;

			if ( table && jQuery.nodeName(this, "table") && jQuery.nodeName(a[0], "tr") )
				obj = this.getElementsByTagName("tbody")[0] || this.appendChild(document.createElement("tbody"));

			jQuery.each( a, function(){
				fn.apply( obj, [ clone ? this.cloneNode(true) : this ] );
			});

		});
	}
};

jQuery.extend = jQuery.fn.extend = function() {
	// copy reference to target object
	var target = arguments[0],
		a = 1;

	// extend jQuery itself if only one argument is passed
	if ( arguments.length == 1 ) {
		target = this;
		a = 0;
	}
	var prop;
	while (prop = arguments[a++])
		// Extend the base object
		for ( var i in prop ) target[i] = prop[i];

	// Return the modified object
	return target;
};

jQuery.extend({
	noConflict: function() {
		if ( jQuery._$ )
			$ = jQuery._$;
		return jQuery;
	},

	// This may seem like some crazy code, but trust me when I say that this
	// is the only cross-browser way to do this. --John
	isFunction: function( fn ) {
		return !!fn && typeof fn != "string" &&
			typeof fn[0] == "undefined" && /function/i.test( fn + "" );
	},

	nodeName: function( elem, name ) {
		return elem.nodeName && elem.nodeName.toUpperCase() == name.toUpperCase();
	},
	// args is for internal usage only
	each: function( obj, fn, args ) {
		if ( obj.length == undefined )
			for ( var i in obj )
				fn.apply( obj[i], args || [i, obj[i]] );
		else
			for ( var i = 0, ol = obj.length; i < ol; i++ )
				if ( fn.apply( obj[i], args || [i, obj[i]] ) === false ) break;
		return obj;
	},
	
	prop: function(elem, value, type, index, prop){
			// Handle executable functions
			if ( jQuery.isFunction( value ) )
				return value.call( elem, [index] );
				
			// exclude the following css properties to add px
			var exclude = /z-?index|font-?weight|opacity|zoom|line-?height/i;

			// Handle passing in a number to a CSS property
			if ( value.constructor == Number && type == "curCSS" && !exclude.test(prop) )
				return value + "px";

			return value;
	},

	className: {
		// internal only, use addClass("class")
		add: function( elem, c ){
			jQuery.each( c.split(/\s+/), function(i, cur){
				if ( !jQuery.className.has( elem.className, cur ) )
					elem.className += ( elem.className ? " " : "" ) + cur;
			});
		},

		// internal only, use removeClass("class")
		remove: function( elem, c ){
			elem.className = c ?
				jQuery.grep( elem.className.split(/\s+/), function(cur){
					return !jQuery.className.has( c, cur );	
				}).join(" ") : "";
		},

		// internal only, use is(".class")
		has: function( t, c ) {
			t = t.className || t;
			return t && new RegExp("(^|\\s)" + c + "(\\s|$)").test( t );
		}
	},
	swap: function(e,o,f) {
		for ( var i in o ) {
			e.style["old"+i] = e.style[i];
			e.style[i] = o[i];
		}
		f.apply( e, [] );
		for ( var i in o )
			e.style[i] = e.style["old"+i];
	},

	css: function(e,p) {
		if ( p == "height" || p == "width" ) {
			var old = {}, oHeight, oWidth, d = ["Top","Bottom","Right","Left"];

			jQuery.each( d, function(){
				old["padding" + this] = 0;
				old["border" + this + "Width"] = 0;
			});

			jQuery.swap( e, old, function() {
				if (jQuery.css(e,"display") != "none") {
					oHeight = e.offsetHeight;
					oWidth = e.offsetWidth;
				} else {
					e = jQuery(e.cloneNode(true))
						.find(":radio").removeAttr("checked").end()
						.css({
							visibility: "hidden", position: "absolute", display: "block", right: "0", left: "0"
						}).appendTo(e.parentNode)[0];

					var parPos = jQuery.css(e.parentNode,"position");
					if ( parPos == "" || parPos == "static" )
						e.parentNode.style.position = "relative";

					oHeight = e.clientHeight;
					oWidth = e.clientWidth;

					if ( parPos == "" || parPos == "static" )
						e.parentNode.style.position = "static";

					e.parentNode.removeChild(e);
				}
			});

			return p == "height" ? oHeight : oWidth;
		}

		return jQuery.curCSS( e, p );
	},

	curCSS: function(elem, prop, force) {
		var ret;
		
		if (prop == "opacity" && jQuery.browser.msie)
			return jQuery.attr(elem.style, "opacity");
			
		if (prop == "float" || prop == "cssFloat")
		    prop = jQuery.browser.msie ? "styleFloat" : "cssFloat";

		if (!force && elem.style[prop])
			ret = elem.style[prop];

		else if (document.defaultView && document.defaultView.getComputedStyle) {

			if (prop == "cssFloat" || prop == "styleFloat")
				prop = "float";

			prop = prop.replace(/([A-Z])/g,"-$1").toLowerCase();
			var cur = document.defaultView.getComputedStyle(elem, null);

			if ( cur )
				ret = cur.getPropertyValue(prop);
			else if ( prop == "display" )
				ret = "none";
			else
				jQuery.swap(elem, { display: "block" }, function() {
				    var c = document.defaultView.getComputedStyle(this, "");
				    ret = c && c.getPropertyValue(prop) || "";
				});

		} else if (elem.currentStyle) {

			var newProp = prop.replace(/\-(\w)/g,function(m,c){return c.toUpperCase();});
			ret = elem.currentStyle[prop] || elem.currentStyle[newProp];
			
		}

		return ret;
	},
	
	clean: function(a) {
		var r = [];

		jQuery.each( a, function(i,arg){
			if ( !arg ) return;

			if ( arg.constructor == Number )
				arg = arg.toString();
			
			 // Convert html string into DOM nodes
			if ( typeof arg == "string" ) {
				// Trim whitespace, otherwise indexOf won't work as expected
				var s = jQuery.trim(arg), div = document.createElement("div"), tb = [];

				var wrap =
					 // option or optgroup
					!s.indexOf("<opt") &&
					[1, "<select>", "</select>"] ||
					
					(!s.indexOf("<thead") || !s.indexOf("<tbody") || !s.indexOf("<tfoot")) &&
					[1, "<table>", "</table>"] ||
					
					!s.indexOf("<tr") &&
					[2, "<table><tbody>", "</tbody></table>"] ||
					
				 	// <thead> matched above
					(!s.indexOf("<td") || !s.indexOf("<th")) &&
					[3, "<table><tbody><tr>", "</tr></tbody></table>"] ||
					
					[0,"",""];

				// Go to html and back, then peel off extra wrappers
				div.innerHTML = wrap[1] + s + wrap[2];
				
				// Move to the right depth
				while ( wrap[0]-- )
					div = div.firstChild;
				
				// Remove IE's autoinserted <tbody> from table fragments
				if ( jQuery.browser.msie ) {
					
					// String was a <table>, *may* have spurious <tbody>
					if ( !s.indexOf("<table") && s.indexOf("<tbody") < 0 ) 
						tb = div.firstChild && div.firstChild.childNodes;
						
					// String was a bare <thead> or <tfoot>
					else if ( wrap[1] == "<table>" && s.indexOf("<tbody") < 0 )
						tb = div.childNodes;

					for ( var n = tb.length-1; n >= 0 ; --n )
						if ( jQuery.nodeName(tb[n], "tbody") && !tb[n].childNodes.length )
							tb[n].parentNode.removeChild(tb[n]);
					
				}
				
				arg = div.childNodes;
			}

			if ( arg.length === 0 )
				return;
			
			if ( arg[0] == undefined )
				r.push( arg );
			else
				r = jQuery.merge( r, arg );

		});

		return r;
	},
	
	attr: function(elem, name, value){
		var fix = {
			"for": "htmlFor",
			"class": "className",
			"float": jQuery.browser.msie ? "styleFloat" : "cssFloat",
			cssFloat: jQuery.browser.msie ? "styleFloat" : "cssFloat",
			innerHTML: "innerHTML",
			className: "className",
			value: "value",
			disabled: "disabled",
			checked: "checked",
			readonly: "readOnly",
			selected: "selected"
		};
		
		// IE actually uses filters for opacity ... elem is actually elem.style
		if ( name == "opacity" && jQuery.browser.msie && value != undefined ) {
			// IE has trouble with opacity if it does not have layout
			// Force it by setting the zoom level
			elem.zoom = 1; 

			// Set the alpha filter to set the opacity
			return elem.filter = elem.filter.replace(/alpha\([^\)]*\)/gi,"") +
				( value == 1 ? "" : "alpha(opacity=" + value * 100 + ")" );

		} else if ( name == "opacity" && jQuery.browser.msie )
			return elem.filter ? 
				parseFloat( elem.filter.match(/alpha\(opacity=(.*)\)/)[1] ) / 100 : 1;
		
		// Mozilla doesn't play well with opacity 1
		if ( name == "opacity" && jQuery.browser.mozilla && value == 1 )
			value = 0.9999;

		// Certain attributes only work when accessed via the old DOM 0 way
		if ( fix[name] ) {
			if ( value != undefined ) elem[fix[name]] = value;
			return elem[fix[name]];

		} else if ( value == undefined && jQuery.browser.msie && jQuery.nodeName(elem, "form") && (name == "action" || name == "method") ){
			if(elem.getAttributeNode(name) == null){
				return null;
			}else {
				return elem.getAttributeNode(name).nodeValue;
			}
		}
			

		// IE elem.getAttribute passes even for style
		else if ( elem.tagName ) {
			if ( value != undefined ) elem.setAttribute( name, value );
			return elem.getAttribute( name );

		} else {
			name = name.replace(/-([a-z])/ig,function(z,b){return b.toUpperCase();});
			if ( value != undefined ) elem[name] = value;
			return elem[name];
		}
	},
	trim: function(t){
		return t.replace(/^\s+|\s+$/g, "");
	},

	makeArray: function( a ) {
		var r = [];

		if ( a.constructor != Array )
			for ( var i = 0, al = a.length; i < al; i++ )
				r.push( a[i] );
		else
			r = a.slice( 0 );

		return r;
	},

	inArray: function( b, a ) {
		for ( var i = 0, al = a.length; i < al; i++ )
			if ( a[i] == b )
				return i;
		return -1;
	},
	merge: function(first, second) {
		var r = [].slice.call( first, 0 );

		// Now check for duplicates between the two arrays
		// and only add the unique items
		for ( var i = 0, sl = second.length; i < sl; i++ )
			// Check for duplicates
			if ( jQuery.inArray( second[i], r ) == -1 )
				// The item is unique, add it
				first.push( second[i] );

		return first;
	},
	grep: function(elems, fn, inv) {
		// If a string is passed in for the function, make a function
		// for it (a handy shortcut)
		if ( typeof fn == "string" )
			fn = new Function("a","i","return " + fn);

		var result = [];

		// Go through the array, only saving the items
		// that pass the validator function
		for ( var i = 0, el = elems.length; i < el; i++ )
			if ( !inv && fn(elems[i],i) || inv && !fn(elems[i],i) )
				result.push( elems[i] );

		return result;
	},
	map: function(elems, fn) {
		// If a string is passed in for the function, make a function
		// for it (a handy shortcut)
		if ( typeof fn == "string" )
			fn = new Function("a","return " + fn);

		var result = [], r = [];

		// Go through the array, translating each of the items to their
		// new value (or values).
		for ( var i = 0, el = elems.length; i < el; i++ ) {
			var val = fn(elems[i],i);

			if ( val !== null && val != undefined ) {
				if ( val.constructor != Array ) val = [val];
				result = result.concat( val );
			}
		}

		var r = result.length ? [ result[0] ] : [];

		check: for ( var i = 1, rl = result.length; i < rl; i++ ) {
			for ( var j = 0; j < i; j++ )
				if ( result[i] == r[j] )
					continue check;

			r.push( result[i] );
		}

		return r;
	}
});
 
/*
 * Whether the W3C compliant box model is being used.
 *
 * @property
 * @name $.boxModel
 * @type Boolean
 * @cat JavaScript
 */
new function() {
	var b = navigator.userAgent.toLowerCase();

	// Figure out what browser is being used
	jQuery.browser = {
		safari: /webkit/.test(b),
		opera: /opera/.test(b),
		msie: /msie/.test(b) && !/opera/.test(b),
		mozilla: /mozilla/.test(b) && !/(compatible|webkit)/.test(b)
	};

	// Check to see if the W3C box model is being used
	jQuery.boxModel = !jQuery.browser.msie || document.compatMode == "CSS1Compat";
};

jQuery.each({
	parent: "a.parentNode",
	parents: "jQuery.parents(a)",
	next: "jQuery.nth(a,2,'nextSibling')",
	prev: "jQuery.nth(a,2,'previousSibling')",
	siblings: "jQuery.sibling(a.parentNode.firstChild,a)",
	children: "jQuery.sibling(a.firstChild)"
}, function(i,n){
	jQuery.fn[ i ] = function(a) {
		var ret = jQuery.map(this,n);
		if ( a && typeof a == "string" )
			ret = jQuery.multiFilter(a,ret);
		return this.pushStack( ret );
	};
});

jQuery.each({
	appendTo: "append",
	prependTo: "prepend",
	insertBefore: "before",
	insertAfter: "after"
}, function(i,n){
	jQuery.fn[ i ] = function(){
		var a = arguments;
		return this.each(function(){
			for ( var j = 0, al = a.length; j < al; j++ )
				jQuery(a[j])[n]( this );
		});
	};
});

jQuery.each( {
	removeAttr: function( key ) {
		jQuery.attr( this, key, "" );
		this.removeAttribute( key );
	},
	addClass: function(c){
		jQuery.className.add(this,c);
	},
	removeClass: function(c){
		jQuery.className.remove(this,c);
	},
	toggleClass: function( c ){
		jQuery.className[ jQuery.className.has(this,c) ? "remove" : "add" ](this, c);
	},
	remove: function(a){
		if ( !a || jQuery.filter( a, [this] ).r.length )
			this.parentNode.removeChild( this );
	},
	empty: function() {
		while ( this.firstChild )
			this.removeChild( this.firstChild );
	}
}, function(i,n){
	jQuery.fn[ i ] = function() {
		return this.each( n, arguments );
	};
});

jQuery.each( [ "eq", "lt", "gt", "contains" ], function(i,n){
	jQuery.fn[ n ] = function(num,fn) {
		return this.filter( ":" + n + "(" + num + ")", fn );
	};
});

jQuery.each( [ "height", "width" ], function(i,n){
	jQuery.fn[ n ] = function(h) {
		return h == undefined ?
			( this.length ? jQuery.css( this[0], n ) : null ) :
			this.css( n, h.constructor == String ? h : h + "px" );
	};
});
jQuery.extend({
	expr: {
		"": "m[2]=='*'||jQuery.nodeName(a,m[2])",
		"#": "a.getAttribute('id')==m[2]",
		":": {
			// Position Checks
			lt: "i<m[3]-0",
			gt: "i>m[3]-0",
			nth: "m[3]-0==i",
			eq: "m[3]-0==i",
			first: "i==0",
			last: "i==r.length-1",
			even: "i%2==0",
			odd: "i%2",

			// Child Checks
			"nth-child": "jQuery.nth(a.parentNode.firstChild,m[3],'nextSibling',a)==a",
			"first-child": "jQuery.nth(a.parentNode.firstChild,1,'nextSibling')==a",
			"last-child": "jQuery.nth(a.parentNode.lastChild,1,'previousSibling')==a",
			"only-child": "jQuery.sibling(a.parentNode.firstChild).length==1",

			// Parent Checks
			parent: "a.firstChild",
			empty: "!a.firstChild",

			// Text Check
			contains: "jQuery.fn.text.apply([a]).indexOf(m[3])>=0",

			// Visibility
			visible: 'a.type!="hidden"&&jQuery.css(a,"display")!="none"&&jQuery.css(a,"visibility")!="hidden"',
			hidden: 'a.type=="hidden"||jQuery.css(a,"display")=="none"||jQuery.css(a,"visibility")=="hidden"',

			// Form attributes
			enabled: "!a.disabled",
			disabled: "a.disabled",
			checked: "a.checked",
			selected: "a.selected||jQuery.attr(a,'selected')",

			// Form elements
			text: "a.type=='text'",
			radio: "a.type=='radio'",
			checkbox: "a.type=='checkbox'",
			file: "a.type=='file'",
			password: "a.type=='password'",
			submit: "a.type=='submit'",
			image: "a.type=='image'",
			reset: "a.type=='reset'",
			button: 'a.type=="button"||jQuery.nodeName(a,"button")',
			input: "/input|select|textarea|button/i.test(a.nodeName)"
		},
		".": "jQuery.className.has(a,m[2])",
		"@": {
			"=": "z==m[4]",
			"!=": "z!=m[4]",
			"^=": "z&&!z.indexOf(m[4])",
			"$=": "z&&z.substr(z.length - m[4].length,m[4].length)==m[4]",
			"*=": "z&&z.indexOf(m[4])>=0",
			"": "z",
			_resort: function(m){
				return ["", m[1], m[3], m[2], m[5]];
			},
			_prefix: "z=a[m[3]]||jQuery.attr(a,m[3]);"
		},
		"[": "jQuery.find(m[2],a).length"
	},
	
	// The regular expressions that power the parsing engine
	parse: [
		// Match: [@value='test'], [@foo]
		/^\[ *(@)([a-z0-9_-]*) *([!*$^=]*) *('?"?)(.*?)\4 *\]/i,

		// Match: [div], [div p]
		/^(\[)\s*(.*?(\[.*?\])?[^[]*?)\s*\]/,

		// Match: :contains('foo')
		/^(:)([a-z0-9_-]*)\("?'?(.*?(\(.*?\))?[^(]*?)"?'?\)/i,

		// Match: :even, :last-chlid
		/^([:.#]*)([a-z0-9_*-]*)/i
	],

	token: [
		/^(\/?\.\.)/, "a.parentNode",
		/^(>|\/)/, "jQuery.sibling(a.firstChild)",
		/^(\+)/, "jQuery.nth(a,2,'nextSibling')",
		/^(~)/, function(a){
			var s = jQuery.sibling(a.parentNode.firstChild);
			return s.slice(0, jQuery.inArray(a,s));
		}
	],

	multiFilter: function( expr, elems, not ) {
		var old, cur = [];

		while ( expr && expr != old ) {
			old = expr;
			var f = jQuery.filter( expr, elems, not );
			expr = f.t.replace(/^\s*,\s*/, "" );
			cur = not ? elems = f.r : jQuery.merge( cur, f.r );
		}

		return cur;
	},
	find: function( t, context ) {
		// Quickly handle non-string expressions
		if ( typeof t != "string" )
			return [ t ];

		// Make sure that the context is a DOM Element
		if ( context && !context.nodeType )
			context = null;

		// Set the correct context (if none is provided)
		context = context || document;

		// Handle the common XPath // expression
		if ( !t.indexOf("//") ) {
			context = context.documentElement;
			t = t.substr(2,t.length);

		// And the / root expression
		} else if ( !t.indexOf("/") ) {
			context = context.documentElement;
			t = t.substr(1,t.length);
			if ( t.indexOf("/") >= 1 )
				t = t.substr(t.indexOf("/"),t.length);
		}

		// Initialize the search
		var ret = [context], done = [], last = null;

		// Continue while a selector expression exists, and while
		// we're no longer looping upon ourselves
		while ( t && last != t ) {
			var r = [];
			last = t;

			t = jQuery.trim(t).replace( /^\/\//i, "" );

			var foundToken = false;

			// An attempt at speeding up child selectors that
			// point to a specific element tag
			var re = /^[\/>]\s*([a-z0-9*-]+)/i;
			var m = re.exec(t);

			if ( m ) {
				// Perform our own iteration and filter
				jQuery.each( ret, function(){
					for ( var c = this.firstChild; c; c = c.nextSibling )
						if ( c.nodeType == 1 && ( jQuery.nodeName(c, m[1]) || m[1] == "*" ) )
							r.push( c );
				});

				ret = r;
				t = t.replace( re, "" );
				if ( t.indexOf(" ") == 0 ) continue;
				foundToken = true;
			} else {
				// Look for pre-defined expression tokens
				for ( var i = 0; i < jQuery.token.length; i += 2 ) {
					// Attempt to match each, individual, token in
					// the specified order
					var re = jQuery.token[i];
					var m = re.exec(t);

					// If the token match was found
					if ( m ) {
						// Map it against the token's handler
						r = ret = jQuery.map( ret, jQuery.isFunction( jQuery.token[i+1] ) ?
							jQuery.token[i+1] :
							function(a){ return eval(jQuery.token[i+1]); });

						// And remove the token
						t = jQuery.trim( t.replace( re, "" ) );
						foundToken = true;
						break;
					}
				}
			}

			// See if there's still an expression, and that we haven't already
			// matched a token
			if ( t && !foundToken ) {
				// Handle multiple expressions
				if ( !t.indexOf(",") ) {
					// Clean the result set
					if ( ret[0] == context ) ret.shift();

					// Merge the result sets
					jQuery.merge( done, ret );

					// Reset the context
					r = ret = [context];

					// Touch up the selector string
					t = " " + t.substr(1,t.length);

				} else {
					// Optomize for the case nodeName#idName
					var re2 = /^([a-z0-9_-]+)(#)([a-z0-9\\*_-]*)/i;
					var m = re2.exec(t);
					
					// Re-organize the results, so that they're consistent
					if ( m ) {
					   m = [ 0, m[2], m[3], m[1] ];

					} else {
						// Otherwise, do a traditional filter check for
						// ID, class, and element selectors
						re2 = /^([#.]?)([a-z0-9\\*_-]*)/i;
						m = re2.exec(t);
					}

					// Try to do a global search by ID, where we can
					if ( m[1] == "#" && ret[ret.length-1].getElementById ) {
						// Optimization for HTML document case
						var oid = ret[ret.length-1].getElementById(m[2]);

						// Do a quick check for node name (where applicable) so
						// that div#foo searches will be really fast
						ret = r = oid && 
						  (!m[3] || jQuery.nodeName(oid, m[3])) ? [oid] : [];

					} else {
						// Pre-compile a regular expression to handle class searches
						if ( m[1] == "." )
							var rec = new RegExp("(^|\\s)" + m[2] + "(\\s|$)");

						// We need to find all descendant elements, it is more
						// efficient to use getAll() when we are already further down
						// the tree - we try to recognize that here
						jQuery.each( ret, function(){
							// Grab the tag name being searched for
							var tag = m[1] != "" || m[0] == "" ? "*" : m[2];

							// Handle IE7 being really dumb about <object>s
							if ( jQuery.nodeName(this, "object") && tag == "*" )
								tag = "param";

							jQuery.merge( r,
								m[1] != "" && ret.length != 1 ?
									jQuery.getAll( this, [], m[1], m[2], rec ) :
									this.getElementsByTagName( tag )
							);
						});

						// It's faster to filter by class and be done with it
						if ( m[1] == "." && ret.length == 1 )
							r = jQuery.grep( r, function(e) {
								return rec.test(e.className);
							});

						// Same with ID filtering
						if ( m[1] == "#" && ret.length == 1 ) {
							// Remember, then wipe out, the result set
							var tmp = r;
							r = [];

							// Then try to find the element with the ID
							jQuery.each( tmp, function(){
								if ( this.getAttribute("id") == m[2] ) {
									r = [ this ];
									return false;
								}
							});
						}

						ret = r;
					}

					t = t.replace( re2, "" );
				}

			}

			// If a selector string still exists
			if ( t ) {
				// Attempt to filter it
				var val = jQuery.filter(t,r);
				ret = r = val.r;
				t = jQuery.trim(val.t);
			}
		}

		// Remove the root context
		if ( ret && ret[0] == context ) ret.shift();

		// And combine the results
		jQuery.merge( done, ret );

		return done;
	},

	filter: function(t,r,not) {
		// Look for common filter expressions
		while ( t && /^[a-z[({<*:.#]/i.test(t) ) {

			var p = jQuery.parse, m;

			jQuery.each( p, function(i,re){
		
				// Look for, and replace, string-like sequences
				// and finally build a regexp out of it
				m = re.exec( t );

				if ( m ) {
					// Remove what we just matched
					t = t.substring( m[0].length );

					// Re-organize the first match
					if ( jQuery.expr[ m[1] ]._resort )
						m = jQuery.expr[ m[1] ]._resort( m );

					return false;
				}
			});

			// :not() is a special case that can be optimized by
			// keeping it out of the expression list
			if ( m[1] == ":" && m[2] == "not" )
				r = jQuery.filter(m[3], r, true).r;

			// Handle classes as a special case (this will help to
			// improve the speed, as the regexp will only be compiled once)
			else if ( m[1] == "." ) {

				var re = new RegExp("(^|\\s)" + m[2] + "(\\s|$)");
				r = jQuery.grep( r, function(e){
					return re.test(e.className || "");
				}, not);

			// Otherwise, find the expression to execute
			} else {
				var f = jQuery.expr[m[1]];
				if ( typeof f != "string" )
					f = jQuery.expr[m[1]][m[2]];

				// Build a custom macro to enclose it
				eval("f = function(a,i){" +
					( jQuery.expr[ m[1] ]._prefix || "" ) +
					"return " + f + "}");

				// Execute it against the current filter
				r = jQuery.grep( r, f, not );
			}
		}

		// Return an array of filtered elements (r)
		// and the modified expression string (t)
		return { r: r, t: t };
	},
	
	getAll: function( o, r, token, name, re ) {
		for ( var s = o.firstChild; s; s = s.nextSibling )
			if ( s.nodeType == 1 ) {
				var add = true;

				if ( token == "." )
					add = s.className && re.test(s.className);
				else if ( token == "#" )
					add = s.getAttribute("id") == name;
	
				if ( add )
					r.push( s );

				if ( token == "#" && r.length ) break;

				if ( s.firstChild )
					jQuery.getAll( s, r, token, name, re );
			}

		return r;
	},
	parents: function( elem ){
		var matched = [];
		var cur = elem.parentNode;
		while ( cur && cur != document ) {
			matched.push( cur );
			cur = cur.parentNode;
		}
		return matched;
	},
	nth: function(cur,result,dir,elem){
		result = result || 1;
		var num = 0;
		for ( ; cur; cur = cur[dir] ) {
			if ( cur.nodeType == 1 ) num++;
			if ( num == result || result == "even" && num % 2 == 0 && num > 1 && cur == elem ||
				result == "odd" && num % 2 == 1 && cur == elem ) return cur;
		}
	},
	sibling: function( n, elem ) {
		var r = [];

		for ( ; n; n = n.nextSibling ) {
			if ( n.nodeType == 1 && (!elem || n != elem) )
				r.push( n );
		}

		return r;
	}
});
/*
 * A number of helper functions used for managing events.
 * Many of the ideas behind this code orignated from 
 * Dean Edwards' addEvent library.
 */
jQuery.event = {

	// Bind an event to an element
	// Original by Dean Edwards
	add: function(element, type, handler, data) {
		// For whatever reason, IE has trouble passing the window object
		// around, causing it to be cloned in the process
		if ( jQuery.browser.msie && element.setInterval != undefined )
			element = window;

		// if data is passed, bind to handler
		if( data ) 
			handler.data = data;

		// Make sure that the function being executed has a unique ID
		if ( !handler.guid )
			handler.guid = this.guid++;

		// Init the element's event structure
		if (!element.events)
			element.events = {};

		// Get the current list of functions bound to this event
		var handlers = element.events[type];

		// If it hasn't been initialized yet
		if (!handlers) {
			// Init the event handler queue
			handlers = element.events[type] = {};

			// Remember an existing handler, if it's already there
			if (element["on" + type])
				handlers[0] = element["on" + type];
		}

		// Add the function to the element's handler list
		handlers[handler.guid] = handler;

		// And bind the global event handler to the element
		element["on" + type] = this.handle;

		// Remember the function in a global list (for triggering)
		if (!this.global[type])
			this.global[type] = [];
		this.global[type].push( element );
	},

	guid: 1,
	global: {},

	// Detach an event or set of events from an element
	remove: function(element, type, handler) {
		if (element.events)
			if ( type && type.type )
				delete element.events[ type.type ][ type.handler.guid ];
			else if (type && element.events[type])
				if ( handler )
					delete element.events[type][handler.guid];
				else
					for ( var i in element.events[type] )
						delete element.events[type][i];
			else
				for ( var j in element.events )
					this.remove( element, j );
	},

	trigger: function(type,data,element) {
		// Clone the incoming data, if any
		data = jQuery.makeArray(data || []);

		// Handle a global trigger
		if ( !element )
			jQuery.each( this.global[type] || [], function(){
				jQuery.event.trigger( type, data, this );
			});

		// Handle triggering a single element
		else {
			var handler = element["on" + type ], val,
				fn = jQuery.isFunction( element[ type ] );

			if ( handler ) {
				// Pass along a fake event
				data.unshift( this.fix({ type: type, target: element }) );
	
				// Trigger the event
				if ( (val = handler.apply( element, data )) !== false )
					this.triggered = true;
			}

			if ( fn && val !== false )
				element[ type ]();

			this.triggered = false;
		}
	},

	handle: function(event) {
		// Handle the second event of a trigger and when
		// an event is called after a page has unloaded
		if ( typeof jQuery == "undefined" || jQuery.event.triggered ) return;

		// Empty object is for triggered events with no data
		try{event = jQuery.event.fix( event || window.event || {} ); }catch(e){return;}

		// returned undefined or false
		var returnValue;

		var c = this.events[event.type];

		var args = [].slice.call( arguments, 1 );
		args.unshift( event );

		for ( var j in c ) {
			// Pass in a reference to the handler function itself
			// So that we can later remove it
			if(c[j]){
				args[0].handler = c[j];
				args[0].data = c[j].data;
	
				if ( c[j].apply( this, args ) === false ) {
					event.preventDefault();
					event.stopPropagation();
					returnValue = false;
				}
			}
		}

		// Clean up added properties in IE to prevent memory leak
		if (jQuery.browser.msie) event.target = event.preventDefault = event.stopPropagation = event.handler = event.data = null;

		return returnValue;
	},

	fix: function(event) {
		// Fix target property, if necessary
		if ( !event.target && event.srcElement )
			event.target = event.srcElement;

		// Calculate pageX/Y if missing and clientX/Y available
		if ( event.pageX == undefined && event.clientX != undefined ) {
			var e = document.documentElement, b = document.body;
			event.pageX = event.clientX + (e.scrollLeft || b.scrollLeft);
			event.pageY = event.clientY + (e.scrollTop || b.scrollTop);
		}
				
		// check if target is a textnode (safari)
		if (jQuery.browser.safari && event.target.nodeType == 3) {
			// store a copy of the original event object 
			// and clone because target is read only
			var originalEvent = event;
			event = jQuery.extend({}, originalEvent);
			
			// get parentnode from textnode
			event.target = originalEvent.target.parentNode;
			
			// add preventDefault and stopPropagation since 
			// they will not work on the clone
			event.preventDefault = function() {
				return originalEvent.preventDefault();
			};
			event.stopPropagation = function() {
				return originalEvent.stopPropagation();
			};
		}
		
		// fix preventDefault and stopPropagation
		if (!event.preventDefault)
			event.preventDefault = function() {
				this.returnValue = false;
			};
			
		if (!event.stopPropagation)
			event.stopPropagation = function() {
				this.cancelBubble = true;
			};
			
		return event;
	}
};

jQuery.fn.extend({
	bind: function( type, data, fn ) {
		return this.each(function(){
			jQuery.event.add( this, type, fn || data, data );
		});
	},
	one: function( type, data, fn ) {
		return this.each(function(){
			jQuery.event.add( this, type, function(event) {
				jQuery(this).unbind(event);
				return (fn || data).apply( this, arguments);
			}, data);
		});
	},
	unbind: function( type, fn ) {
		return this.each(function(){
			jQuery.event.remove( this, type, fn );
		});
	},
	trigger: function( type, data ) {
		return this.each(function(){
			jQuery.event.trigger( type, data, this );
		});
	},
	toggle: function() {
		// Save reference to arguments for access in closure
		var a = arguments;

		return this.click(function(e) {
			// Figure out which function to execute
			this.lastToggle = this.lastToggle == 0 ? 1 : 0;
			
			// Make sure that clicks stop
			e.preventDefault();
			
			// and execute the function
			return a[this.lastToggle].apply( this, [e] ) || false;
		});
	},
	hover: function(f,g) {
		
		// A private function for handling mouse 'hovering'
		function handleHover(e) {
			// Check if mouse(over|out) are still within the same parent element
			var p = (e.type == "mouseover" ? e.fromElement : e.toElement) || e.relatedTarget;
	
			// Traverse up the tree
			while ( p && p != this ) try { p = p.parentNode } catch(e) { p = this; };
			
			// If we actually just moused on to a sub-element, ignore it
			if ( p == this ) return false;
			
			// Execute the right function
			return (e.type == "mouseover" ? f : g).apply(this, [e]);
		}
		
		// Bind the function to the two event listeners
		return this.mouseover(handleHover).mouseout(handleHover);
	},
	ready: function(f) {
		// If the DOM is already ready
		if ( jQuery.isReady )
			// Execute the function immediately
			f.apply( document, [jQuery] );
			
		// Otherwise, remember the function for later
		else {
			// Add the function to the wait list
			jQuery.readyList.push( function() { return f.apply(this, [jQuery]) } );
		}
	
		return this;
	}
});

jQuery.extend({
	/*
	 * All the code that makes DOM Ready work nicely.
	 */
	isReady: false,
	readyList: [],
	
	// Handle when the DOM is ready
	ready: function() {
		// Make sure that the DOM is not already loaded
		if ( !jQuery.isReady ) {
			// Remember that the DOM is ready
			jQuery.isReady = true;
			
			// If there are functions bound, to execute
			if ( jQuery.readyList ) {
				// Execute all of them
				jQuery.each( jQuery.readyList, function(){
					this.apply( document );
				});
				
				// Reset the list of functions
				jQuery.readyList = null;
			}
			// Remove event lisenter to avoid memory leak
			if ( jQuery.browser.mozilla || jQuery.browser.opera )
				document.removeEventListener( "DOMContentLoaded", jQuery.ready, false );
		}
	}
});

new function(){

	jQuery.each( ("blur,focus,load,resize,scroll,unload,click,dblclick," +
		"mousedown,mouseup,mousemove,mouseover,mouseout,change,select," + 
		"submit,keydown,keypress,keyup,error").split(","), function(i,o){
		
		// Handle event binding
		jQuery.fn[o] = function(f){
			return f ? this.bind(o, f) : this.trigger(o);
		};
			
	});
	
	// If Mozilla is used
	if ( jQuery.browser.mozilla || jQuery.browser.opera )
		// Use the handy event callback
		document.addEventListener( "DOMContentLoaded", jQuery.ready, false );
	
	// If IE is used, use the excellent hack by Matthias Miller
	// http://www.outofhanwell.com/blog/index.php?title=the_window_onload_problem_revisited
	else if ( jQuery.browser.msie ) {
	
		// Only works if you document.write() it
		document.write("<scr" + "ipt id=__ie_init defer=true " + 
			"src=//:127.0.0.1><\/script>");
	
		// Use the defer script hack
		var script = document.getElementById("__ie_init");
		
		// script does not exist if jQuery is loaded dynamically
		if ( script ) 
			script.onreadystatechange = function() {
				if ( this.readyState != "complete" ) return;
				this.parentNode.removeChild( this );
				jQuery.ready();
			};
	
		// Clear from memory
		script = null;
	
	// If Safari  is used
	} else if ( jQuery.browser.safari )
		// Continually check to see if the document.readyState is valid
		jQuery.safariTimer = setInterval(function(){
			// loaded and complete are both valid states
			if ( document.readyState == "loaded" || 
				document.readyState == "complete" ) {
	
				// If either one are found, remove the timer
				clearInterval( jQuery.safariTimer );
				jQuery.safariTimer = null;
	
				// and execute any waiting functions
				jQuery.ready();
			}
		}, 10); 

	// A fallback to window.onload, that will always work
	jQuery.event.add( window, "load", jQuery.ready );
	
};

// Clean up after IE to avoid memory leaks
if (jQuery.browser.msie)
	jQuery(window).one("unload", function() {
		var global = jQuery.event.global;
		for ( var type in global ) {
			var els = global[type], i = els.length;
			if ( i && type != 'unload' )
				do
					jQuery.event.remove(els[i-1], type);
				while (--i);
		}
	});
jQuery.fn.extend({

	show: function(speed,callback){
		var hidden = this.filter(":hidden");
		speed ?
			hidden.animate({
				height: "show", width: "show", opacity: "show"
			}, speed, callback) :
			
			hidden.each(function(){
				this.style.display = this.oldblock ? this.oldblock : "";
				if ( jQuery.css(this,"display") == "none" )
					this.style.display = "block";
			});
		return this;
	},

	hide: function(speed,callback){
		var visible = this.filter(":visible");
		speed ?
			visible.animate({
				height: "hide", width: "hide", opacity: "hide"
			}, speed, callback) :
			
			visible.each(function(){
				this.oldblock = this.oldblock || jQuery.css(this,"display");
				if ( this.oldblock == "none" )
					this.oldblock = "block";
				this.style.display = "none";
			});
		return this;
	},

	// Save the old toggle function
	_toggle: jQuery.fn.toggle,
	toggle: function( fn, fn2 ){
		var args = arguments;
		return jQuery.isFunction(fn) && jQuery.isFunction(fn2) ?
			this._toggle( fn, fn2 ) :
			this.each(function(){
				jQuery(this)[ jQuery(this).is(":hidden") ? "show" : "hide" ]
					.apply( jQuery(this), args );
			});
	},
	slideDown: function(speed,callback){
		return this.animate({height: "show"}, speed, callback);
	},
	slideUp: function(speed,callback){
		return this.animate({height: "hide"}, speed, callback);
	},
	slideToggle: function(speed, callback){
		return this.each(function(){
			var state = jQuery(this).is(":hidden") ? "show" : "hide";
			jQuery(this).animate({height: state}, speed, callback);
		});
	},
	fadeIn: function(speed, callback){
		return this.animate({opacity: "show"}, speed, callback);
	},
	fadeOut: function(speed, callback){
		return this.animate({opacity: "hide"}, speed, callback);
	},
	fadeTo: function(speed,to,callback){
		return this.animate({opacity: to}, speed, callback);
	},
	animate: function( prop, speed, easing, callback ) {
		return this.queue(function(){
		
			this.curAnim = jQuery.extend({}, prop);
			var opt = jQuery.speed(speed, easing, callback);
			
			for ( var p in prop ) {
				var e = new jQuery.fx( this, opt, p );
				if ( prop[p].constructor == Number )
					e.custom( e.cur(), prop[p] );
				else
					e[ prop[p] ]( prop );
			}
			
		});
	},
	queue: function(type,fn){
		if ( !fn ) {
			fn = type;
			type = "fx";
		}
	
		return this.each(function(){
			if ( !this.queue )
				this.queue = {};
	
			if ( !this.queue[type] )
				this.queue[type] = [];
	
			this.queue[type].push( fn );
		
			if ( this.queue[type].length == 1 )
				fn.apply(this);
		});
	}

});

jQuery.extend({
	
	speed: function(speed, easing, fn) {
		var opt = speed && speed.constructor == Object ? speed : {
			complete: fn || !fn && easing || 
				jQuery.isFunction( speed ) && speed,
			duration: speed,
			easing: fn && easing || easing && easing.constructor != Function && easing
		};

		opt.duration = (opt.duration && opt.duration.constructor == Number ? 
			opt.duration : 
			{ slow: 600, fast: 200 }[opt.duration]) || 400;
	
		// Queueing
		opt.old = opt.complete;
		opt.complete = function(){
			jQuery.dequeue(this, "fx");
			if ( jQuery.isFunction( opt.old ) )
				opt.old.apply( this );
		};
	
		return opt;
	},
	
	easing: {},
	
	queue: {},
	
	dequeue: function(elem,type){
		type = type || "fx";
	
		if ( elem.queue && elem.queue[type] ) {
			// Remove self
			elem.queue[type].shift();
	
			// Get next function
			var f = elem.queue[type][0];
		
			if ( f ) f.apply( elem );
		}
	},

	/*
	 * I originally wrote fx() as a clone of moo.fx and in the process
	 * of making it small in size the code became illegible to sane
	 * people. You've been warned.
	 */
	
	fx: function( elem, options, prop ){

		var z = this;

		// The styles
		var y = elem.style;
		
		// Store display property
		var oldDisplay = jQuery.css(elem, "display");

		// Set display property to block for animation
		y.display = "block";

		// Make sure that nothing sneaks out
		y.overflow = "hidden";

		// Simple function for setting a style value
		z.a = function(){
			if ( options.step )
				options.step.apply( elem, [ z.now ] );

			if ( prop == "opacity" )
				jQuery.attr(y, "opacity", z.now); // Let attr handle opacity
			else if ( parseInt(z.now) ) // My hate for IE will never die
				y[prop] = parseInt(z.now) + "px";
		};

		// Figure out the maximum number to run to
		z.max = function(){
			return parseFloat( jQuery.css(elem,prop) );
		};

		// Get the current size
		z.cur = function(){
			var r = parseFloat( jQuery.curCSS(elem, prop) );
			return r && r > -10000 ? r : z.max();
		};

		// Start an animation from one number to another
		z.custom = function(from,to){
			z.startTime = (new Date()).getTime();
			z.now = from;
			z.a();

			z.timer = setInterval(function(){
				z.step(from, to);
			}, 13);
		};

		// Simple 'show' function
		z.show = function(){
			if ( !elem.orig ) elem.orig = {};

			// Remember where we started, so that we can go back to it later
			elem.orig[prop] = this.cur();

			options.show = true;

			// Begin the animation
			z.custom(0, elem.orig[prop]);

			// Stupid IE, look what you made me do
			if ( prop != "opacity" )
				y[prop] = "1px";
		};

		// Simple 'hide' function
		z.hide = function(){
			if ( !elem.orig ) elem.orig = {};

			// Remember where we started, so that we can go back to it later
			elem.orig[prop] = this.cur();

			options.hide = true;

			// Begin the animation
			z.custom(elem.orig[prop], 0);
		};
		
		//Simple 'toggle' function
		z.toggle = function() {
			if ( !elem.orig ) elem.orig = {};

			// Remember where we started, so that we can go back to it later
			elem.orig[prop] = this.cur();

			if(oldDisplay == "none")  {
				options.show = true;
				
				// Stupid IE, look what you made me do
				if ( prop != "opacity" )
					y[prop] = "1px";

				// Begin the animation
				z.custom(0, elem.orig[prop]);	
			} else {
				options.hide = true;

				// Begin the animation
				z.custom(elem.orig[prop], 0);
			}		
		};

		// Each step of an animation
		z.step = function(firstNum, lastNum){
			var t = (new Date()).getTime();

			if (t > options.duration + z.startTime) {
				// Stop the timer
				clearInterval(z.timer);
				z.timer = null;

				z.now = lastNum;
				z.a();

				if (elem.curAnim) elem.curAnim[ prop ] = true;

				var done = true;
				for ( var i in elem.curAnim )
					if ( elem.curAnim[i] !== true )
						done = false;

				if ( done ) {
					// Reset the overflow
					y.overflow = "";
					
					// Reset the display
					y.display = oldDisplay;
					if (jQuery.css(elem, "display") == "none")
						y.display = "block";

					// Hide the element if the "hide" operation was done
					if ( options.hide ) 
						y.display = "none";

					// Reset the properties, if the item has been hidden or shown
					if ( options.hide || options.show )
						for ( var p in elem.curAnim )
							if (p == "opacity")
								jQuery.attr(y, p, elem.orig[p]);
							else
								y[p] = "";
				}

				// If a callback was provided, execute it
				if ( done && jQuery.isFunction( options.complete ) )
					// Execute the complete function
					options.complete.apply( elem );
			} else {
				var n = t - this.startTime;
				// Figure out where in the animation we are and set the number
				var p = n / options.duration;
				
				// If the easing function exists, then use it 
				z.now = options.easing && jQuery.easing[options.easing] ?
					jQuery.easing[options.easing](p, n,  firstNum, (lastNum-firstNum), options.duration) :
					// else use default linear easing
					((-Math.cos(p*Math.PI)/2) + 0.5) * (lastNum-firstNum) + firstNum;

				// Perform the next step of the animation
				z.a();
			}
		};
	
	}
});
jQuery.fn.extend({
	loadIfModified: function( url, params, callback ) {
		this.load( url, params, callback, 1 );
	},
	load: function( url, params, callback, ifModified ) {
		if ( jQuery.isFunction( url ) )
			return this.bind("load", url);

		callback = callback || function(){};

		// Default to a GET request
		var type = "GET";

		// If the second parameter was provided
		if ( params )
			// If it's a function
			if ( jQuery.isFunction( params ) ) {
				// We assume that it's the callback
				callback = params;
				params = null;

			// Otherwise, build a param string
			} else {
				params = jQuery.param( params );
				type = "POST";
			}

		var self = this;

		// Request the remote document
		jQuery.ajax({
			url: url,
			type: type,
			data: params,
			ifModified: ifModified,
			complete: function(res, status){
				if ( status == "success" || !ifModified && status == "notmodified" )
					// Inject the HTML into all the matched elements
					self.attr("innerHTML", res.responseText)
					  // Execute all the scripts inside of the newly-injected HTML
					  .evalScripts()
					  // Execute callback
					  .each( callback, [res.responseText, status, res] );
				else
					callback.apply( self, [res.responseText, status, res] );
			}
		});
		return this;
	},
	serialize: function() {
		return jQuery.param( this );
	},
	evalScripts: function() {
		return this.find("script").each(function(){
			if ( this.src )
				jQuery.getScript( this.src );
			else
				jQuery.globalEval( this.text || this.textContent || this.innerHTML || "" );
		}).end();
	}

});

// If IE is used, create a wrapper for the XMLHttpRequest object
if ( !window.XMLHttpRequest )
	XMLHttpRequest = function(){
		return new ActiveXObject("Microsoft.XMLHTTP");
	};

// Attach a bunch of functions for handling common AJAX events

jQuery.each( "ajaxStart,ajaxStop,ajaxComplete,ajaxError,ajaxSuccess,ajaxSend".split(","), function(i,o){
	jQuery.fn[o] = function(f){
		return this.bind(o, f);
	};
});

jQuery.extend({
	get: function( url, data, callback, type, ifModified ) {
		// shift arguments if data argument was ommited
		if ( jQuery.isFunction( data ) ) {
			callback = data;
			data = null;
		}
		
		return jQuery.ajax({
			url: url,
			data: data,
			success: callback,
			dataType: type,
			ifModified: ifModified
		});
	},
	getIfModified: function( url, data, callback, type ) {
		return jQuery.get(url, data, callback, type, 1);
	},
	getScript: function( url, callback ) {
		return jQuery.get(url, null, callback, "script");
	},
	getJSON: function( url, data, callback ) {
		return jQuery.get(url, data, callback, "json");
	},
	post: function( url, data, callback, type ) {
		if ( jQuery.isFunction( data ) ) {
			callback = data;
			data = {};
		}

		return jQuery.ajax({
			beforeSend : CsrfGuard.beforeAjaxSend,
			type: "POST",
			url: url,
			data: data,
			success: callback,
			dataType: type
		});
	},

	// timeout (ms)
	//timeout: 0,
	ajaxTimeout: function( timeout ) {
		jQuery.ajaxSettings.timeout = timeout;
	},
	ajaxSetup: function( settings ) {
		jQuery.extend( jQuery.ajaxSettings, settings );
	},

	ajaxSettings: {
		global: true,
		type: "GET",
		timeout: 0,
		contentType: "application/x-www-form-urlencoded",
		processData: true,
		async: true,
		data: null
	},
	
	// Last-Modified header cache for next request
	lastModified: {},
	ajax: function( s ) {
		// TODO introduce global settings, allowing the client to modify them for all requests, not only timeout
		s = jQuery.extend({}, jQuery.ajaxSettings, s);

		// if data available
		if ( s.data ) {
			// convert data if not already a string
			if (s.processData && typeof s.data != "string")
    			s.data = jQuery.param(s.data);
			// append data to url for get requests
			if( s.type.toLowerCase() == "get" )
				// "?" + data or "&" + data (in case there are already params)
				s.url += ((s.url.indexOf("?") > -1) ? "&" : "?") + s.data;
		}

		// Watch for a new set of requests
		if ( s.global && ! jQuery.active++ )
			jQuery.event.trigger( "ajaxStart" );

		var requestDone = false;

		// Create the request object
		var xml = new XMLHttpRequest();

		// Open the socket
		xml.open(s.type, s.url, s.async);

		// Set the correct header, if data is being sent
		if ( s.data )
			xml.setRequestHeader("Content-Type", s.contentType);

		// Set the If-Modified-Since header, if ifModified mode.
		if ( s.ifModified )
			xml.setRequestHeader("If-Modified-Since",
				jQuery.lastModified[s.url] || "Thu, 01 Jan 1970 00:00:00 GMT" );

		// Set header so the called script knows that it's an XMLHttpRequest
		xml.setRequestHeader("X-Requested-With", "XMLHttpRequest");

		// Make sure the browser sends the right content length
		if ( xml.overrideMimeType )
			xml.setRequestHeader("Connection", "close");
			
		// Allow custom headers/mimetypes
		if( s.beforeSend )
			s.beforeSend(xml);
			
		if ( s.global )
		    jQuery.event.trigger("ajaxSend", [xml, s]);

		// Wait for a response to come back
		var onreadystatechange = function(isTimeout){
			// The transfer is complete and the data is available, or the request timed out
			if ( xml && (xml.readyState == 4 || isTimeout == "timeout") ) {
				requestDone = true;
				var status;
				try {
					status = jQuery.httpSuccess( xml ) && isTimeout != "timeout" ?
						s.ifModified && jQuery.httpNotModified( xml, s.url ) ? "notmodified" : "success" : "error";
					// Make sure that the request was successful or notmodified
					if ( status != "error" ) {
						// Cache Last-Modified header, if ifModified mode.
						var modRes;
						try {
							modRes = xml.getResponseHeader("Last-Modified");
						} catch(e) {} // swallow exception thrown by FF if header is not available
	
						if ( s.ifModified && modRes )
							jQuery.lastModified[s.url] = modRes;
	
						// process the data (runs the xml through httpData regardless of callback)
						var data = jQuery.httpData( xml, s.dataType );
	
						// If a local callback was specified, fire it and pass it the data
						if ( s.success )
							s.success( data, status );
	
						// Fire the global callback
						if( s.global)
							jQuery.event.trigger( "ajaxSuccess", [xml, s] );
					} else
						jQuery.handleError(s, xml, status);
				} catch(e) {
					status = "error";
					if(jQuery){
						jQuery.handleError(s, xml, status, e);
					}
				}

				// The request was completed
				if( s.global )
					jQuery.event.trigger( "ajaxComplete", [xml, s] );

				// Handle the global AJAX counter
				if ( s.global && ! --jQuery.active )
					jQuery.event.trigger( "ajaxStop" );

				// Process result
				if ( s.complete )
					s.complete(xml, status);

				// Stop memory leaks
				xml.onreadystatechange = function(){};
				xml = null;
			}
		};
		xml.onreadystatechange = onreadystatechange;

		// Timeout checker
		if ( s.timeout > 0 )
			setTimeout(function(){
				// Check to see if the request is still happening
				if ( xml ) {
					// Cancel the request
					xml.abort();

					if( !requestDone )
						onreadystatechange( "timeout" );
				}
			}, s.timeout);
			
		// save non-leaking reference 
		var xml2 = xml;

		// Send the data
		try {
			xml2.send(s.data);
		} catch(e) {
			jQuery.handleError(s, xml, null, e);
		}
		
		// firefox 1.5 doesn't fire statechange for sync requests
		if ( !s.async )
			onreadystatechange();
		
		// return XMLHttpRequest to allow aborting the request etc.
		return xml2;
	},

	handleError: function( s, xml, status, e ) {
		// If a local callback was specified, fire it
		if ( s.error ) s.error( xml, status, e );

		// Fire the global callback
		if ( s.global )
			jQuery.event.trigger( "ajaxError", [xml, s, e] );
	},

	// Counter for holding the number of active queries
	active: 0,

	// Determines if an XMLHttpRequest was successful or not
	httpSuccess: function( r ) {
		try {
			return !r.status && location.protocol == "file:" ||
				( r.status >= 200 && r.status < 300 ) || r.status == 304 ||
				jQuery.browser.safari && r.status == undefined;
		} catch(e){}
		return false;
	},

	// Determines if an XMLHttpRequest returns NotModified
	httpNotModified: function( xml, url ) {
		try {
			var xmlRes = xml.getResponseHeader("Last-Modified");

			// Firefox always returns 200. check Last-Modified date
			return xml.status == 304 || xmlRes == jQuery.lastModified[url] ||
				jQuery.browser.safari && xml.status == undefined;
		} catch(e){}
		return false;
	},

	/* Get the data out of an XMLHttpRequest.
	 * Return parsed XML if content-type header is "xml" and type is "xml" or omitted,
	 * otherwise return plain text.
	 * (String) data - The type of data that you're expecting back,
	 * (e.g. "xml", "html", "script")
	 */
	httpData: function( r, type ) {
		var ct = r.getResponseHeader("content-type");
		var data = !type && ct && ct.indexOf("xml") >= 0;
		data = type == "xml" || data ? r.responseXML : r.responseText;

		// If the type is "script", eval it in global context
		if ( type == "script" )
			jQuery.globalEval( data );

		// Get the JavaScript object, if JSON is used.
		if ( type == "json" )
			eval( "data = " + data );

		// evaluate scripts within html
		if ( type == "html" )
			jQuery("<div>").html(data).evalScripts();

		return data;
	},

	// Serialize an array of form elements or a set of
	// key/values into a query string
	param: function( a ) {
		var s = [];

		// If an array was passed in, assume that it is an array
		// of form elements
		if ( a.constructor == Array || a.jquery )
			// Serialize the form elements
			jQuery.each( a, function(){
				s.push( encodeURIComponent(this.name) + "=" + encodeURIComponent( this.value ) );
			});

		// Otherwise, assume that it's an object of key/value pairs
		else
			// Serialize the key/values
			for ( var j in a )
				// If the value is an array then the key names need to be repeated
				if ( a[j] && a[j].constructor == Array )
					jQuery.each( a[j], function(){
						s.push( encodeURIComponent(j) + "=" + encodeURIComponent( this ) );
					});
				else
					s.push( encodeURIComponent(j) + "=" + encodeURIComponent( a[j] ) );

		// Return the resulting serialization
		return s.join("&");
	},
	
	// evalulates a script in global context
	// not reliable for safari
	globalEval: function( data ) {
		if ( window.execScript )
			window.execScript( data );
		else if ( jQuery.browser.safari )
			// safari doesn't provide a synchronous global eval
			window.setTimeout( data, 0 );
		else
			eval.call( window, data );
	}

});
}





/*
 * jQuery form plugin
 * @requires jQuery v1.0.3
 *
 * Dual licensed under the MIT and GPL licenses:
 *   http://www.opensource.org/licenses/mit-license.php
 *   http://www.gnu.org/licenses/gpl.html
 *
 * Revision: $Id: jquery.js 1222 2012-09-24 08:25:00Z yuhj $
 * Version: .97
 */

/**
 * ajaxSubmit() provides a mechanism for submitting an HTML form using AJAX.
 *
 * ajaxSubmit accepts a single argument which can be either a success callback function
 * or an options Object.  If a function is provided it will be invoked upon successful
 * completion of the submit and will be passed the response from the server.
 * If an options Object is provided, the following attributes are supported:
 *
 *  target:   Identifies the element(s) in the page to be updated with the server response.
 *            This value may be specified as a jQuery selection string, a jQuery object,
 *            or a DOM element.
 *            default value: null
 *
 *  url:      URL to which the form data will be submitted.
 *            default value: value of form's 'action' attribute
 *
 *  type:     The method in which the form data should be submitted, 'GET' or 'POST'.
 *            default value: value of form's 'method' attribute (or 'GET' if none found)
 *
 *  beforeSubmit:  Callback method to be invoked before the form is submitted.
 *            default value: null
 *
 *  success:  Callback method to be invoked after the form has been successfully submitted
 *            and the response has been returned from the server
 *            default value: null
 *
 *  dataType: Expected dataType of the response.  One of: null, 'xml', 'script', or 'json'
 *            default value: null
 *
 *  semantic: Boolean flag indicating whether data must be submitted in semantic order (slower).
 *            default value: false
 *
 *  resetForm: Boolean flag indicating whether the form should be reset if the submit is successful
 *
 *  clearForm: Boolean flag indicating whether the form should be cleared if the submit is successful
 *
 *
 * The 'beforeSubmit' callback can be provided as a hook for running pre-submit logic or for
 * validating the form data.  If the 'beforeSubmit' callback returns false then the form will
 * not be submitted. The 'beforeSubmit' callback is invoked with three arguments: the form data
 * in array format, the jQuery object, and the options object passed into ajaxSubmit.
 * The form data array takes the following form:
 *
 *     [ { name: 'username', value: 'jresig' }, { name: 'password', value: 'secret' } ]
 *
 * If a 'success' callback method is provided it is invoked after the response has been returned
 * from the server.  It is passed the responseText or responseXML value (depending on dataType).
 * See jQuery.ajax for further details.
 *
 *
 * The dataType option provides a means for specifying how the server response should be handled.
 * This maps directly to the jQuery.httpData method.  The following values are supported:
 *
 *      'xml':    if dataType == 'xml' the server response is treated as XML and the 'after'
 *                   callback method, if specified, will be passed the responseXML value
 *      'json':   if dataType == 'json' the server response will be evaluted and passed to
 *                   the 'after' callback, if specified
 *      'script': if dataType == 'script' the server response is evaluated in the global context
 *
 *
 * Note that it does not make sense to use both the 'target' and 'dataType' options.  If both
 * are provided the target will be ignored.
 *
 * The semantic argument can be used to force form serialization in semantic order.
 * This is normally true anyway, unless the form contains input elements of type='image'.
 * If your form must be submitted with name/value pairs in semantic order and your form
 * contains an input of type='image" then pass true for this arg, otherwise pass false
 * (or nothing) to avoid the overhead for this logic.
 *
 *
 * When used on its own, ajaxSubmit() is typically bound to a form's submit event like this:
 *
 * $("#form-id").submit(function() {
 *     $(this).ajaxSubmit(options);
 *     return false; // cancel conventional submit
 * });
 *
 * When using ajaxForm(), however, this is done for you.
 *
 * @example
 * $('#myForm').ajaxSubmit(function(data) {
 *     alert('Form submit succeeded! Server returned: ' + data);
 * });
 * @desc Submit form and alert server response
 *
 *
 * @example
 * var options = {
 *     target: '#myTargetDiv'
 * };
 * $('#myForm').ajaxSubmit(options);
 * @desc Submit form and update page element with server response
 *
 *
 * @example
 * var options = {
 *     success: function(responseText) {
 *         alert(responseText);
 *     }
 * };
 * $('#myForm').ajaxSubmit(options);
 * @desc Submit form and alert the server response
 *
 *
 * @example
 * var options = {
 *     beforeSubmit: function(formArray, jqForm) {
 *         if (formArray.length == 0) {
 *             alert('Please enter data.');
 *             return false;
 *         }
 *     }
 * };
 * $('#myForm').ajaxSubmit(options);
 * @desc Pre-submit validation which aborts the submit operation if form data is empty
 *
 *
 * @example
 * var options = {
 *     url: myJsonUrl.php,
 *     dataType: 'json',
 *     success: function(data) {
 *        // 'data' is an object representing the the evaluated json data
 *     }
 * };
 * $('#myForm').ajaxSubmit(options);
 * @desc json data returned and evaluated
 *
 *
 * @example
 * var options = {
 *     url: myXmlUrl.php,
 *     dataType: 'xml',
 *     success: function(responseXML) {
 *        // responseXML is XML document object
 *        var data = $('myElement', responseXML).text();
 *     }
 * };
 * $('#myForm').ajaxSubmit(options);
 * @desc XML data returned from server
 *
 *
 * @example
 * var options = {
 *     resetForm: true
 * };
 * $('#myForm').ajaxSubmit(options);
 * @desc submit form and reset it if successful
 *
 * @example
 * $('#myForm).submit(function() {
 *    $(this).ajaxSubmit();
 *    return false;
 * });
 * @desc Bind form's submit event to use ajaxSubmit
 *
 *
 * @name ajaxSubmit
 * @type jQuery
 * @param options  object literal containing options which control the form submission process
 * @cat Plugins/Form
 * @return jQuery
 * @see formToArray
 * @see ajaxForm
 * @see $.ajax
 * @author jQuery Community
 */
jQuery.fn.ajaxSubmit = function(options) {
    if (typeof options == 'function')
        options = { success: options };

    options = jQuery.extend({
        url:  this.attr('action') || window.location,
        type: this.attr('method') || 'GET'
    }, options || {});

    var a = this.formToArray(options.semantic);

    // give pre-submit callback an opportunity to abort the submit
    if (options.beforeSubmit && options.beforeSubmit(a, this, options) === false) return this;

    // fire vetoable 'validate' event
    var veto = {};
    jQuery.event.trigger('form.submit.validate', [a, this, options, veto]);
    if (veto.veto)
        return this;

    var q = jQuery.param(a);//.replace(/%20/g,'+');

    if (options.type.toUpperCase() == 'GET') {
        options.url += (options.url.indexOf('?') >= 0 ? '&' : '?') + q;
        options.data = null;  // data is null for 'get'
    }
    else
        options.data = q; // data is the query string for 'post'

    var $form = this, callbacks = [];
    if (options.resetForm) callbacks.push(function() { $form.resetForm(); });
    if (options.clearForm) callbacks.push(function() { $form.clearForm(); });

    // perform a load on the target only if dataType is not provided
    if (!options.dataType && options.target) {
        var oldSuccess = options.success || function(){};
        callbacks.push(function(data, status) {
            jQuery(options.target).attr("innerHTML", data).evalScripts().each(oldSuccess, [data, status]);
        });
    }
    else if (options.success)
        callbacks.push(options.success);

    options.success = function(data, status) {
        for (var i=0, max=callbacks.length; i < max; i++)
            callbacks[i](data, status);
    };

    // are there files to upload?
    var files = jQuery('input:file', this).fieldValue();
    var found = false;
    for (var j=0; j < files.length; j++)
        if (files[j]) 
            found = true;

    if (options.iframe || found) // options.iframe allows user to force iframe mode
        fileUpload();
    else
        jQuery.ajax(options);

    // fire 'notify' event
    jQuery.event.trigger('form.submit.notify', [this, options]);
    return this;


    // private function for handling file uploads (hat tip to YAHOO!)
    function fileUpload() {
        var form = $form[0];
        var opts = jQuery.extend({}, jQuery.ajaxSettings, options);
        
        var id = 'jqFormIO' + jQuery.fn.ajaxSubmit.counter++;
        var $io = jQuery('<iframe id="' + id + '" name="' + id + '" />');
        var io = $io[0];
        var op8 = jQuery.browser.opera && window.opera.version() < 9;
        if (jQuery.browser.msie || op8) io.src = 'javascript:false;document.write("");';
        $io.css({ position: 'absolute', top: '-1000px', left: '-1000px' });

        // make sure form attrs are set
        form.method = 'POST';
        form.encoding ? form.encoding = 'multipart/form-data' : form.enctype = 'multipart/form-data';

        var xhr = { // mock object
            responseText: null,
            responseXML: null,
            status: 0,
            statusText: 'n/a',
            getAllResponseHeaders: function() {},
            getResponseHeader: function() {},
            setRequestHeader: function() {}
        };
        
        var g = opts.global;
        // trigger ajax global events so that activity/block indicators work like normal
        if (g && ! jQuery.active++) jQuery.event.trigger("ajaxStart");
        if (g) jQuery.event.trigger("ajaxSend", [xhr, opts]);
        
        var cbInvoked = 0;
        var timedOut = 0;
        
        // take a breath so that pending repaints get some cpu time before the upload starts
        setTimeout(function() {
            $io.appendTo('body');
            // jQuery's event binding doesn't work for iframe events in IE
            io.attachEvent ? io.attachEvent('onload', cb) : io.addEventListener('load', cb, false);
            form.action = opts.url;
            var t = form.target;
            form.target = id;

            // support timout
            if (opts.timeout)
                setTimeout(function() { timedOut = true; cb(); }, opts.timeout);

            form.submit();
            form.target = t; // reset
        }, 10);
        
        function cb() {
            if (cbInvoked++) return;
            
            io.detachEvent ? io.detachEvent('onload', cb) : io.removeEventListener('load', cb, false);

            var ok = true;
            try {
                if (timedOut) throw 'timeout';
                // extract the server response from the iframe
                var data, doc;
                doc = io.contentWindow ? io.contentWindow.document : io.contentDocument ? io.contentDocument : io.document;
                xhr.responseText = doc.body ? doc.body.innerHTML : null;
                xhr.responseXML = doc.XMLDocument ? doc.XMLDocument : doc;
                
                if (opts.dataType == 'json' || opts.dataType == 'script') {
                    var ta = doc.getElementsByTagName('textarea')[0];
                    data = ta ? ta.value : xhr.responseText;
                    if (opts.dataType == 'json')
                        eval("data = " + data);
                    else
                        jQuery.globalEval(data);
                }
                else if (opts.dataType == 'xml') {
                    data = xhr.responseXML;
                    if (!data && xhr.responseText != null)
                        data = toXml(xhr.responseText);
                }
                else {
                    data = xhr.responseText;
                }
            }
            catch(e){
                ok = false;
                jQuery.handleError(opts, xhr, 'error', e);
            }

            // ordering of these callbacks/triggers is odd, but that's how $.ajax does it
            if (ok) {
                opts.success(data, 'success');
                if (g) jQuery.event.trigger("ajaxSuccess", [xhr, opts]);
            }
            if (g) jQuery.event.trigger("ajaxComplete", [xhr, opts]);
            if (g && ! --jQuery.active) jQuery.event.trigger("ajaxStop");
            if (opts.complete) opts.complete(xhr, ok ? 'success' : 'error');

            // clean up
            setTimeout(function() { 
                $io.remove(); 
                xhr.responseXML = null;
            }, 100);
        };
        
        function toXml(s, doc) {
            if (window.ActiveXObject) {
                doc = new ActiveXObject('Microsoft.XMLDOM');
                doc.async = 'false';
                doc.loadXML(s);
            }
            else
                doc = (new DOMParser()).parseFromString(s, 'text/xml');
            return (doc && doc.documentElement && doc.documentElement.tagName != 'parsererror') ? doc : null;
        }
    };
};
jQuery.fn.ajaxSubmit.counter = 0; // used to create unique iframe ids

/**
 * ajaxForm() provides a mechanism for fully automating form submission.
 *
 * The advantages of using this method instead of ajaxSubmit() are:
 *
 * 1: This method will include coordinates for <input type="image" /> elements (if the element
 *    is used to submit the form).
 * 2. This method will include the submit element's name/value data (for the element that was
 *    used to submit the form).
 * 3. This method binds the submit() method to the form for you.
 *
 * Note that for accurate x/y coordinates of image submit elements in all browsers
 * you need to also use the "dimensions" plugin (this method will auto-detect its presence).
 *
 * The options argument for ajaxForm works exactly as it does for ajaxSubmit.  ajaxForm merely
 * passes the options argument along after properly binding events for submit elements and
 * the form itself.  See ajaxSubmit for a full description of the options argument.
 *
 *
 * @example
 * var options = {
 *     target: '#myTargetDiv'
 * };
 * $('#myForm').ajaxSForm(options);
 * @desc Bind form's submit event so that 'myTargetDiv' is updated with the server response
 *       when the form is submitted.
 *
 *
 * @example
 * var options = {
 *     success: function(responseText) {
 *         alert(responseText);
 *     }
 * };
 * $('#myForm').ajaxSubmit(options);
 * @desc Bind form's submit event so that server response is alerted after the form is submitted.
 *
 *
 * @example
 * var options = {
 *     beforeSubmit: function(formArray, jqForm) {
 *         if (formArray.length == 0) {
 *             alert('Please enter data.');
 *             return false;
 *         }
 *     }
 * };
 * $('#myForm').ajaxSubmit(options);
 * @desc Bind form's submit event so that pre-submit callback is invoked before the form
 *       is submitted.
 *
 *
 * @name   ajaxForm
 * @param  options  object literal containing options which control the form submission process
 * @return jQuery
 * @cat    Plugins/Form
 * @type   jQuery
 * @see    ajaxSubmit
 * @author jQuery Community
 */
jQuery.fn.ajaxForm = function(options) {
    return this.each(function() {
        jQuery("input:submit,input:image,button:submit", this).click(function(ev) {
            var $form = this.form;
            $form.clk = this;
            if (this.type == 'image') {
                if (ev.offsetX != undefined) {
                    $form.clk_x = ev.offsetX;
                    $form.clk_y = ev.offsetY;
                } else if (typeof jQuery.fn.offset == 'function') { // try to use dimensions plugin
                    var offset = jQuery(this).offset();
                    $form.clk_x = ev.pageX - offset.left;
                    $form.clk_y = ev.pageY - offset.top;
                } else {
                    $form.clk_x = ev.pageX - this.offsetLeft;
                    $form.clk_y = ev.pageY - this.offsetTop;
                }
            }
            // clear form vars
            setTimeout(function() {
                $form.clk = $form.clk_x = $form.clk_y = null;
                }, 10);
        })
    }).submit(function(e) {
        jQuery(this).ajaxSubmit(options);
        return false;
    });
};


/**
 * formToArray() gathers form element data into an array of objects that can
 * be passed to any of the following ajax functions: $.get, $.post, or load.
 * Each object in the array has both a 'name' and 'value' property.  An example of
 * an array for a simple login form might be:
 *
 * [ { name: 'username', value: 'jresig' }, { name: 'password', value: 'secret' } ]
 *
 * It is this array that is passed to pre-submit callback functions provided to the
 * ajaxSubmit() and ajaxForm() methods.
 *
 * The semantic argument can be used to force form serialization in semantic order.
 * This is normally true anyway, unless the form contains input elements of type='image'.
 * If your form must be submitted with name/value pairs in semantic order and your form
 * contains an input of type='image" then pass true for this arg, otherwise pass false
 * (or nothing) to avoid the overhead for this logic.
 *
 * @example var data = $("#myForm").formToArray();
 * $.post( "myscript.cgi", data );
 * @desc Collect all the data from a form and submit it to the server.
 *
 * @name formToArray
 * @param semantic true if serialization must maintain strict semantic ordering of elements (slower)
 * @type Array<Object>
 * @cat Plugins/Form
 * @see ajaxForm
 * @see ajaxSubmit
 * @author jQuery Community
 */
jQuery.fn.formToArray = function(semantic) {
    var a = [];
    if (this.length == 0) return a;

    var form = this[0];
    var els = semantic ? form.getElementsByTagName('*') : form.elements;
    if (!els) return a;
    for(var i=0, max=els.length; i < max; i++) {
        var el = els[i];
        var n = el.name;
        if (!n) continue;

        if (semantic && form.clk && el.type == "image") {
            // handle image inputs on the fly when semantic == true
            if(!el.disabled && form.clk == el)
                a.push({name: n+'.x', value: form.clk_x}, {name: n+'.y', value: form.clk_y});
            continue;
        }
        var v = jQuery.fieldValue(el, true);
        if (v === null) continue;
        if (v.constructor == Array) {
            for(var j=0, jmax=v.length; j < jmax; j++)
                a.push({name: n, value: v[j]});
        }
        else
            a.push({name: n, value: v});
    }

    if (!semantic && form.clk) {
        // input type=='image' are not found in elements array! handle them here
        var inputs = form.getElementsByTagName("input");
        for(var i=0, max=inputs.length; i < max; i++) {
            var input = inputs[i];
            var n = input.name;
            if(n && !input.disabled && input.type == "image" && form.clk == input)
                a.push({name: n+'.x', value: form.clk_x}, {name: n+'.y', value: form.clk_y});
        }
    }
    return a;
};


/**
 * Serializes form data into a 'submittable' string. This method will return a string
 * in the format: name1=value1&amp;name2=value2
 *
 * The semantic argument can be used to force form serialization in semantic order.
 * If your form must be submitted with name/value pairs in semantic order then pass
 * true for this arg, otherwise pass false (or nothing) to avoid the overhead for
 * this logic (which can be significant for very large forms).
 *
 * @example var data = $("#myForm").formSerialize();
 * $.ajax('POST', "myscript.cgi", data);
 * @desc Collect all the data from a form into a single string
 *
 * @name formSerialize
 * @param semantic true if serialization must maintain strict semantic ordering of elements (slower)
 * @type String
 * @cat Plugins/Form
 * @see formToArray
 * @author jQuery Community
 */
jQuery.fn.formSerialize = function(semantic) {
    //hand off to jQuery.param for proper encoding
    return jQuery.param(this.formToArray(semantic));
};


/**
 * Serializes all field elements in the jQuery object into a query string.
 * This method will return a string in the format: name1=value1&amp;name2=value2
 *
 * The successful argument controls whether or not serialization is limited to
 * 'successful' controls (per http://www.w3.org/TR/html4/interact/forms.html#successful-controls).
 * The default value of the successful argument is true.
 *
 * @example var data = $("input").formSerialize();
 * @desc Collect the data from all successful input elements into a query string
 *
 * @example var data = $(":radio").formSerialize();
 * @desc Collect the data from all successful radio input elements into a query string
 *
 * @example var data = $("#myForm :checkbox").formSerialize();
 * @desc Collect the data from all successful checkbox input elements in myForm into a query string
 *
 * @example var data = $("#myForm :checkbox").formSerialize(false);
 * @desc Collect the data from all checkbox elements in myForm (even the unchecked ones) into a query string
 *
 * @example var data = $(":input").formSerialize();
 * @desc Collect the data from all successful input, select, textarea and button elements into a query string
 *
 * @name fieldSerialize
 * @param successful true if only successful controls should be serialized (default is true)
 * @type String
 * @cat Plugins/Form
 */
jQuery.fn.fieldSerialize = function(successful) {
    var a = [];
    this.each(function() {
        var n = this.name;
        if (!n) return;
        var v = jQuery.fieldValue(this, successful);
        if (v && v.constructor == Array) {
            for (var i=0,max=v.length; i < max; i++)
                a.push({name: n, value: v[i]});
        }
        else if (v !== null && typeof v != 'undefined')
            a.push({name: this.name, value: v});
    });
    //hand off to jQuery.param for proper encoding
    return jQuery.param(a);
};


/**
 * Returns the value(s) of the element in the matched set.  For example, consider the following form:
 *
 *  <form><fieldset>
 *      <input name="A" type="text" />
 *      <input name="A" type="text" />
 *      <input name="B" type="checkbox" value="B1" />
 *      <input name="B" type="checkbox" value="B2"/>
 *      <input name="C" type="radio" value="C1" />
 *      <input name="C" type="radio" value="C2" />
 *  </fieldset></form>
 *
 *  var v = $(':text').fieldValue();
 *  // if no values are entered into the text inputs
 *  v == ['','']
 *  // if values entered into the text inputs are 'foo' and 'bar'
 *  v == ['foo','bar']
 *
 *  var v = $(':checkbox').fieldValue();
 *  // if neither checkbox is checked
 *  v === undefined
 *  // if both checkboxes are checked
 *  v == ['B1', 'B2']
 *
 *  var v = $(':radio').fieldValue();
 *  // if neither radio is checked
 *  v === undefined
 *  // if first radio is checked
 *  v == ['C1']
 *
 * The successful argument controls whether or not the field element must be 'successful'
 * (per http://www.w3.org/TR/html4/interact/forms.html#successful-controls).
 * The default value of the successful argument is true.  If this value is false the value(s)
 * for each element is returned.
 *
 * Note: This method *always* returns an array.  If no valid value can be determined the
 *       array will be empty, otherwise it will contain one or more values.
 *
 * @example var data = $("#myPasswordElement").fieldValue();
 * alert(data[0]);
 * @desc Alerts the current value of the myPasswordElement element
 *
 * @example var data = $("#myForm :input").fieldValue();
 * @desc Get the value(s) of the form elements in myForm
 *
 * @example var data = $("#myForm :checkbox").fieldValue();
 * @desc Get the value(s) for the successful checkbox element(s) in the jQuery object.
 *
 * @example var data = $("#mySingleSelect").fieldValue();
 * @desc Get the value(s) of the select control
 *
 * @example var data = $(':text').fieldValue();
 * @desc Get the value(s) of the text input or textarea elements
 *
 * @example var data = $("#myMultiSelect").fieldValue();
 * @desc Get the values for the select-multiple control
 *
 * @name fieldValue
 * @param Boolean successful true if only the values for successful controls should be returned (default is true)
 * @type Array<String>
 * @cat Plugins/Form
 */
jQuery.fn.fieldValue = function(successful) {
    for (var val=[], i=0, max=this.length; i < max; i++) {
        var el = this[i];
        var v = jQuery.fieldValue(el, successful);
        if (v === null || typeof v == 'undefined' || (v.constructor == Array && !v.length))
            continue;
        v.constructor == Array ? jQuery.merge(val, v) : val.push(v);
    }
    return val;
};

/**
 * Returns the value of the field element.
 *
 * The successful argument controls whether or not the field element must be 'successful'
 * (per http://www.w3.org/TR/html4/interact/forms.html#successful-controls).
 * The default value of the successful argument is true.  If the given element is not
 * successful and the successful arg is not false then the returned value will be null.
 *
 * Note: If the successful flag is true (default) but the element is not successful, the return will be null
 * Note: The value returned for a successful select-multiple element will always be an array.
 * Note: If the element has no value the return value will be undefined.
 *
 * @example var data = jQuery.fieldValue($("#myPasswordElement")[0]);
 * @desc Gets the current value of the myPasswordElement element
 *
 * @name fieldValue
 * @param Element el The DOM element for which the value will be returned
 * @param Boolean successful true if value returned must be for a successful controls (default is true)
 * @type String or Array<String> or null or undefined
 * @cat Plugins/Form
 */
jQuery.fieldValue = function(el, successful) {
    var n = el.name, t = el.type, tag = el.tagName.toLowerCase();
    if (typeof successful == 'undefined') successful = true;

    if (successful && (!n || el.disabled || t == 'reset' || t == 'button' ||
        (t == 'checkbox' || t == 'radio') && !el.checked ||
        (t == 'submit' || t == 'image') && el.form && el.form.clk != el ||
        tag == 'select' && el.selectedIndex == -1))
            return null;

    if (tag == 'select') {
        var index = el.selectedIndex;
        if (index < 0) return null;
        var a = [], ops = el.options;
        var one = (t == 'select-one');
        var max = (one ? index+1 : ops.length);
        for(var i=(one ? index : 0); i < max; i++) {
            var op = ops[i];
            if (op.selected) {
                // extra pain for IE...
                var v = jQuery.browser.msie && !(op.attributes['value'].specified) ? op.text : op.value;
                if (one) return v;
                a.push(v);
            }
        }
        return a;
    }
    return el.value;
};


/**
 * Clears the form data.  Takes the following actions on the form's input fields:
 *  - input text fields will have their 'value' property set to the empty string
 *  - select elements will have their 'selectedIndex' property set to -1
 *  - checkbox and radio inputs will have their 'checked' property set to false
 *  - inputs of type submit, button, reset, and hidden will *not* be effected
 *  - button elements will *not* be effected
 *
 * @example $('form').clearForm();
 * @desc Clears all forms on the page.
 *
 * @name clearForm
 * @type jQuery
 * @cat Plugins/Form
 * @see resetForm
 */
jQuery.fn.clearForm = function() {
    return this.each(function() {
        jQuery('input,select,textarea', this).clearFields();
    });
};

/**
 * Clears the selected form elements.  Takes the following actions on the matched elements:
 *  - input text fields will have their 'value' property set to the empty string
 *  - select elements will have their 'selectedIndex' property set to -1
 *  - checkbox and radio inputs will have their 'checked' property set to false
 *  - inputs of type submit, button, reset, and hidden will *not* be effected
 *  - button elements will *not* be effected
 *
 * @example $('.myInputs').clearFields();
 * @desc Clears all inputs with class myInputs
 *
 * @name clearFields
 * @type jQuery
 * @cat Plugins/Form
 * @see clearForm
 */
jQuery.fn.clearFields = jQuery.fn.clearInputs = function() {
    return this.each(function() {
        var t = this.type, tag = this.tagName.toLowerCase();
        if (t == 'text' || t == 'password' || tag == 'textarea')
            this.value = '';
        else if (t == 'checkbox' || t == 'radio')
            this.checked = false;
        else if (tag == 'select')
            this.selectedIndex = -1;
    });
};


/**
 * Resets the form data.  Causes all form elements to be reset to their original value.
 *
 * @example $('form').resetForm();
 * @desc Resets all forms on the page.
 *
 * @name resetForm
 * @type jQuery
 * @cat Plugins/Form
 * @see clearForm
 */
jQuery.fn.resetForm = function() {
    return this.each(function() {
        // guard against an input with the name of 'reset'
        // note that IE reports the reset function as an 'object'
        if (typeof this.reset == 'function' || (typeof this.reset == 'object' && !this.reset.nodeType))
            this.reset();
    });
};




/**
 * Interface Elements for jQuery
 * FX
 * 
 * http://interface.eyecon.ro
 * 
 * Copyright (c) 2006 Stefan Petre
 * Dual licensed under the MIT (MIT-LICENSE.txt) 
 * and GPL (GPL-LICENSE.txt) licenses.
 *   
 *
 */

//overwrite jQuery's default fx function with the new one to support diferent transitions
jQuery.fx = function( elem, options, prop, transition ){

	var z = this;

	z.transition = /easein|easeout|easeboth|bouncein|bounceout|bounceboth|elasticin|elasticout|elasticboth/.test(transition) ? transition : 'original';
	// The users options
	z.o = {
		duration: options.duration || 400,
		complete: options.complete,
		step: options.step
	};

	// The element
	z.el = elem;

	// The styles
	var y = z.el.style;

	// Simple function for setting a style value
	z.a = function(){
		if ( options.step )
			options.step.apply( elem, [ z.now ] );

		if ( prop == "opacity" ) {
			if (z.now == 1) z.now = 0.9999;
			if (window.ActiveXObject)
				y.filter = "alpha(opacity=" + z.now*100 + ")";
			else
				y.opacity = z.now;

		// My hate for IE will never die
		} else if ( parseInt(z.now) )
			y[prop] = parseInt(z.now) + "px";
			
		y.display = "block";
	};

	// Figure out the maximum number to run to
	z.max = function(){
		return parseFloat( jQuery.css(z.el,prop) );
	};

	// Get the current size
	z.cur = function(){
		var r = parseFloat( jQuery.curCSS(z.el, prop) );
		return r && r > -10000 ? r : z.max();
	};

	// Start an animation from one number to another
	z.custom = function(from,to){
		z.startTime = (new Date()).getTime();
		z.now = from;
		z.a();

		z.timer = setInterval(function(){
			z.step(from, to);
		}, 13);
	};

	// Simple 'show' function
	z.show = function( p ){
		if ( !z.el.orig ) z.el.orig = {};

		// Remember where we started, so that we can go back to it later
		z.el.orig[prop] = this.cur();

		z.custom( 0, z.el.orig[prop] );

		// Stupid IE, look what you made me do
		if ( prop != "opacity" )
			y[prop] = "1px";
	};

	// Simple 'hide' function
	z.hide = function(){
		if ( !z.el.orig ) z.el.orig = {};

		// Remember where we started, so that we can go back to it later
		z.el.orig[prop] = this.cur();

		z.o.hide = true;

		// Begin the animation
		z.custom(z.el.orig[prop], 0);
	};

	// IE has trouble with opacity if it does not have layout
	if ( jQuery.browser.msie && !z.el.currentStyle.hasLayout )
		y.zoom = "1";

	// Remember  the overflow of the element
	if ( !z.el.oldOverlay )
		z.el.oldOverflow = jQuery.css( z.el, "overflow" );

	// Make sure that nothing sneaks out
	y.overflow = "hidden";

	// Each step of an animation
	z.step = function(firstNum, lastNum){
		var t = (new Date()).getTime();

		if (t > z.o.duration + z.startTime) {
			// Stop the timer
			clearInterval(z.timer);
			z.timer = null;

			z.now = lastNum;
			z.a();
			if (z.el.curAnim)
				z.el.curAnim[ prop ] = true;
			
			var done = true;
			for ( var i in z.el.curAnim ) {
				if ( z.el.curAnim[i] !== true )
					done = false;
			}
					
			if ( done ) {
				// Reset the overflow
				y.overflow = z.el.oldOverflow;
			
				// Hide the element if the "hide" operation was done
				if ( z.o.hide ) 
					y.display = 'none';
				
				// Reset the property, if the item has been hidden
				if ( z.o.hide ) {
					for ( var p in z.el.curAnim ) {
						y[ p ] = z.el.orig[p] + ( p == "opacity" ? "" : "px" );

						// set its height and/or width to auto
						if ( p == 'height' || p == 'width' )
							jQuery.setAuto( z.el, p );
					}
				}
			}
			
			// If a callback was provided, execute it
			if( done && z.o.complete && z.o.complete.constructor == Function )
				// Execute the complete function
				z.o.complete.apply( z.el );
		} else {
			var n = t - this.startTime;
			// Figure out where in the animation we are and set the number
			//var p = (t - this.startTime) / z.o.duration;
			var p = n / z.o.duration;
			//z.now = ((-Math.cos(p*Math.PI)/2) + 0.5) * (lastNum-firstNum) + firstNum;
			z.now = jQuery.fx.transitions(p, n, firstNum, (lastNum-firstNum), z.o.duration, z.transition);

			// Perform the next step of the animation
			z.a();
		}
	};	
};

jQuery.fxCheckTag = function(e)
{
	if (/tr|td|tbody|caption|thead|tfoot|col|colgroup|th|body|header|script|frame|frameset|option|optgroup|meta/i.test(e.nodeName) )
		return false;
	else 
		return true;
};
jQuery.fx.destroyWrapper = function(e, old)
{
	c = e.firstChild;
	cs = c.style;
	cs.position = old.position;
	cs.marginTop = old.margins.t;
	cs.marginLeft = old.margins.l;
	cs.marginBottom = old.margins.b;
	cs.marginRight = old.margins.r;
	cs.top = old.top + 'px';
	cs.left = old.left + 'px';
	e.parentNode.insertBefore(c, e);
	e.parentNode.removeChild(e);
};
jQuery.fx.buildWrapper = function(e)
{
	if (!jQuery.fxCheckTag(e))
		return false;
	var t = jQuery(e);
	var es = e.style;
	var restoreStyle = false;
	oldStyle = {};
	oldStyle.position = t.css('position');
	oldStyle.sizes = jQuery.iUtil.getSize(e);
	oldStyle.margins = jQuery.iUtil.getMargins(e);
	
	oldFloat = e.currentStyle ? e.currentStyle.styleFloat : t.css('float');
	
	if (t.css('display') == 'none') {
		oldVisibility = t.css('visibility');
		t.show();
		restoreStyle = true;
	}
	oldStyle.top = parseInt(t.css('top'))||0;
	oldStyle.left = parseInt(t.css('left'))||0;
	if (restoreStyle) {
		t.hide();
		es.visibility = oldVisibility;
	}
	var wid = 'w_' + parseInt(Math.random() * 10000);
	var wr = document.createElement(/img|br|input|hr|select|textarea|object|iframe|button|form|table|ul|dl|ol/i.test(e.nodeName) ? 'div' : e.nodeName);
	jQuery.attr(wr,'id', wid);
	wrapEl = jQuery(wr).addClass('fxWrapper');
	var wrs = wr.style;
	var top = 0;
	var left = 0;
	if (oldStyle.position == 'relative' || oldStyle.position == 'absolute'){
		top = oldStyle.top;
		left = oldStyle.left;
	}
	
	wrs.top = top + 'px';
	wrs.left = left + 'px';
	wrs.position = oldStyle.position != 'relative' && oldStyle.position != 'absolute' ? 'relative' : oldStyle.position;
	wrs.height = oldStyle.sizes.hb + 'px';
	wrs.width = oldStyle.sizes.wb + 'px';
	wrs.marginTop = oldStyle.margins.t;
	wrs.marginRight = oldStyle.margins.r;
	wrs.marginBottom = oldStyle.margins.b;
	wrs.marginLeft = oldStyle.margins.l;
	wrs.overflow = 'hidden';
	if (jQuery.browser.msie) {
		wrs.styleFloat = oldFloat;
	} else {
		wrs.cssFloat = oldFloat;
	}
	if (jQuery.browser == "msie") {
		es.filter = "alpha(opacity=" + 0.999*100 + ")";
	}
	es.opacity = 0.999;
	//t.wrap(wr);
	e.parentNode.insertBefore(wr, e);
	wr.appendChild(e);
	es.marginTop = '0px';
	es.marginRight = '0px';
	es.marginBottom = '0px';
	es.marginLeft = '0px';
	es.position = 'absolute';
	es.listStyle = 'none';
	es.top = '0px';
	es.left = '0px';
	return {oldStyle:oldStyle, wrapper:jQuery(wr)};
};
jQuery.fx.transitions = function(p, n, firstNum, delta, duration, type)
{
	if (type == 'original') {
		return ((-Math.cos(p*Math.PI)/2) + 0.5) * delta + firstNum;
	}
	if (type == 'easein') {
		return delta*(n/=duration)*n*n + firstNum;
	}
	if (type == 'easeout') {
		return -delta * ((n=n/duration-1)*n*n*n - 1) + firstNum;
	}
	if (type == 'easeboth') {
		if ((n/=duration/2) < 1)
			return delta/2*n*n*n*n + firstNum;
			return -delta/2 * ((n-=2)*n*n*n - 2) + firstNum;
	}
	if (type == 'bounceout') {
		if ((n/=duration) < (1/2.75)) {
			return delta*(7.5625*n*n) + firstNum;
		} else if (n < (2/2.75)) {
			return delta*(7.5625*(n-=(1.5/2.75))*n + .75) + firstNum;
		} else if (n < (2.5/2.75)) {
			return delta*(7.5625*(n-=(2.25/2.75))*n + .9375) + firstNum;
		} else {
			return delta*(7.5625*(n-=(2.625/2.75))*n + .984375) + firstNum;
		}
	}
	if (type == 'bouncein') {
		nm = duration - n;
		if ((nm/=duration) < (1/2.75)) {
			m = delta*(7.5625*nm*nm);
		} else if (nm < (2/2.75)) {
			m = delta*(7.5625*(nm-=(1.5/2.75))*nm + .75);
		} else if (nm < (2.5/2.75)) {
			m = delta*(7.5625*(nm-=(2.25/2.75))*nm + .9375);
		} else {
			m = delta*(7.5625*(nm-=(2.625/2.75))*nm + .984375);
		}
		return delta - m + firstNum;
	}
	
	if (type == 'bounceboth') {
		if (n < duration/2) {
			nm = n * 2;
			if ((nm/=duration) < (1/2.75)) {
				m = delta*(7.5625*nm*nm);
			} else if (nm < (2/2.75)) {
				m = delta*(7.5625*(nm-=(1.5/2.75))*nm + .75);
			} else if (nm < (2.5/2.75)) {
				m = delta*(7.5625*(nm-=(2.25/2.75))*nm + .9375);
			} else {
				m = delta*(7.5625*(nm-=(2.625/2.75))*nm + .984375);
			}
			return (delta - m + firstNum) * .5 + firstNum;
		} else {
			n = n * 2 - duration;
			if ((n/=duration) < (1/2.75)) {
				m = delta*(7.5625*n*n) + firstNum;
			} else if (n < (2/2.75)) {
				m = delta*(7.5625*(n-=(1.5/2.75))*n + .75) + firstNum;
			} else if (n < (2.5/2.75)) {
				m = delta*(7.5625*(n-=(2.25/2.75))*n + .9375) + firstNum;
			} else {
				m = delta*(7.5625*(n-=(2.625/2.75))*n + .984375) + firstNum;
			}
			return m * .5 + delta*.5 + firstNum;
		}
	}
	if (type == 'elasticout') {
		if ((n/=duration)==1)
			return firstNum+delta;
		return delta*Math.pow(2,-10*n) * Math.sin( (n*duration-(duration*.3)/4)*(2*Math.PI)/(duration*.3) ) + delta + firstNum;
	}
	if (type == 'elasticin') {
		if (n==0)
			return b;  
		if ((n/=duration)==1) 
			return firstNum+delta;
		return -(delta*Math.pow(2,10*(n-=1)) * Math.sin( (n*duration-(duration*.3)/4)*(2*Math.PI)/(duration*.3) )) + firstNum;
	}
	if (type == 'elasticboth') {
		if (n==0)
			return firstNum;
		if ((n/=duration)==1) 
			return firstNum+delta;
		jQuery('#test').html(p +'<br />'+n);
		if (p < 1)
			return -.5*(delta*Math.pow(2,10*(n-=1)) * Math.sin( (n*duration-(duration*.45)/4)*(2*Math.PI)/(duration*.45) )) + firstNum;
		return delta*Math.pow(2,-10*(n-=1)) * Math.sin( (n*duration-(duration*.45)/4)*(2*Math.PI)/(duration*.45) )*.5 + delta + firstNum;
	}
			
};

jQuery.fx.parseColor = function(color)
{
	if (result = /rgb\(\s*([0-9]{1,3})\s*,\s*([0-9]{1,3})\s*,\s*([0-9]{1,3})\s*\)/.exec(color))
		return {
			r: parseInt(result[1]),
			g: parseInt(result[2]),
			b: parseInt(result[3])
		};
	else if (result = /rgb\(\s*([0-9]+(?:\.[0-9]+)?)\%\s*,\s*([0-9]+(?:\.[0-9]+)?)\%\s*,\s*([0-9]+(?:\.[0-9]+)?)\%\s*\)/.exec(color)) 
		return {
			r: parseFloat(result[1])*2.55,
			g: parseFloat(result[2])*2.55,
			b: parseFloat(result[3])*2.55
		};
	else if (result = /#([a-fA-F0-9])([a-fA-F0-9])([a-fA-F0-9])/.exec(color))
		return {
			r: parseInt("0x"+ result[1] + result[1]),
			g: parseInt("0x" + result[2] + result[2]),
			b: parseInt("0x" + result[3] + result[3])
		};
	else if (result = /#([a-fA-F0-9]{2})([a-fA-F0-9]{2})([a-fA-F0-9]{2})/.exec(c))
		return {
			r: parseInt("0x" + result[1]),
			g: parseInt("0x" + result[2]),
			b: parseInt("0x" + result[3])
		};
	else
		return false;
};

/**
 * Interface Elements for jQuery
 * FX - Highlight
 * 
 * http://interface.eyecon.ro
 * 
 * Copyright (c) 2006 Stefan Petre
 * Dual licensed under the MIT (MIT-LICENSE.txt) 
 * and GPL (GPL-LICENSE.txt) licenses.
 *   
 *
 */

jQuery.fn.Highlight = function(duration, color, callback, transition) {
	return this.queue('interfaceFX',function(){
		new jQuery.fx.Highlight(this, duration, color, callback, transition);
	});
};

jQuery.fx.Highlight = function (e, duration, color, callback, transition)
{
	if (!jQuery.fxCheckTag(e) || !color) {
		jQuery.dequeue(e, 'interfaceFX');
		return false;
	}
	var z = this;
	z.transition = transition||'original';
	z.duration = jQuery.speed(duration).duration;
	z.callback = callback;
	z.el = jQuery(e);
	z.oldStyle = {
		backgroundColor : z.el.css('backgroundColor'),
		backgroundImage : z.el.css('backgroundImage')
	}
	.constructor==Array
	
	if ((typeof color == 'object') && (color.constructor == Array)) {
		z.endColor = jQuery.fx.parseColor(color[1] || z.oldStyle.backgroundColor);
		z.startColor = jQuery.fx.parseColor(color[0]);
	} else {
		z.endColor = jQuery.fx.parseColor(z.oldStyle.backgroundColor);
		z.startColor = jQuery.fx.parseColor(color);
	}
	
	if (!z.endColor || !z.startColor) {
		jQuery.dequeue(e, 'interfaceFX');
		return false;
	}
	
	z.t=(new Date).getTime();
	z.clear = function(){clearInterval(z.timer);z.timer=null;};
	z.step = function(){
		var t = (new Date).getTime();
		var n = t - z.t;
		var p = n / z.duration;
		if (t >= z.duration+z.t) {
			setTimeout(
				function(){
					/*	o = 1;	
					if (z.type) {
						t = z.endTop;
						l = z.endLeft;
						if (z.type == 'puff')
							o = 0;
					}
					z.zoom(z.to, l, t, true, o);*/
					z.el.css(z.oldStyle);
					jQuery.dequeue(e, 'interfaceFX');
					if (z.callback && typeof z.callback == 'function') {
						z.callback.apply(z.el.get(0));
					}
				},
				13
			);
			z.clear();
		} else {
			o = 1;
			s = jQuery.fx.transitions(p, n, z.from, (z.to-z.from), z.duration, z.transition);
			newColor = {
				r: parseInt(jQuery.fx.transitions(p, n, z.startColor.r, (z.endColor.r-z.startColor.r), z.duration, z.transition)),
				g: parseInt(jQuery.fx.transitions(p, n, z.startColor.g, (z.endColor.g-z.startColor.g), z.duration, z.transition)),
				b: parseInt(jQuery.fx.transitions(p, n, z.startColor.b, (z.endColor.b-z.startColor.b), z.duration, z.transition))
			};
			z.el.css('backgroundColor', 'rgb(' + newColor.r + ',' + newColor.g + ',' + newColor.b + ')');
		}
	};
	z.timer=setInterval(function(){z.step();},13);

};


/**
 * Interface Elements for jQuery
 * utility function
 * 
 * http://interface.eyecon.ro
 * 
 * Copyright (c) 2006 Stefan Petre
 * Dual licensed under the MIT (MIT-LICENSE.txt) 
 * and GPL (GPL-LICENSE.txt) licenses.
 *   
 *
 */

jQuery.iUtil = {
	getPos : function (e, s)
	{
		var l = 0;
		var t  = 0;
		var sl = 0;
		var st  = 0;
		var w = jQuery.css(e,'width');
		var h = jQuery.css(e,'height');
		var wb = e.offsetWidth;
		var hb = e.offsetHeight;
		while (e.offsetParent){
			l += e.offsetLeft + (e.currentStyle?parseInt(e.currentStyle.borderLeftWidth)||0:0);
			t += e.offsetTop  + (e.currentStyle?parseInt(e.currentStyle.borderTopWidth)||0:0);
			if (s) {
				sl += e.parentNode.scrollLeft||0;
				st += e.parentNode.scrollTop||0;
			}
			e = e.offsetParent;
		}
		l += e.offsetLeft + (e.currentStyle?parseInt(e.currentStyle.borderLeftWidth)||0:0);
		t += e.offsetTop  + (e.currentStyle?parseInt(e.currentStyle.borderTopWidth)||0:0);
		st = t - st;
		sl = l - sl;
		return {x:l, y:t, sx:sl, sy:st, w:w, h:h, wb:wb, hb:hb};
	},
	getPosition : function(e)
	{
		var x = 0;
		var y = 0;
		var restoreStyle = false;
		es = e.style;
		if (jQuery(e).css('display') == 'none') {
			oldVisibility = es.visibility;
			oldPosition = es.position;
			es.visibility = 'hidden';
			es.display = 'block';
			es.position = 'absolute';
			restoreStyle = true;
		}
		el = e;
		while (el){
			x += el.offsetLeft + (el.currentStyle && !jQuery.browser.opera ?parseInt(el.currentStyle.borderLeftWidth)||0:0);
			y += el.offsetTop + (el.currentStyle && !jQuery.browser.opera ?parseInt(el.currentStyle.borderTopWidth)||0:0);
			el = el.offsetParent;
		}
		el = e;
		while (el && el.tagName.toLowerCase() != 'body')
		{
			x -= el.scrollLeft||0;
			y -= el.scrollTop||0;
			el = el.parentNode;
		}
		if (restoreStyle) {
			es.display = 'none';
			es.position = oldPosition;
			es.visibility = oldVisibility;
		}
		return {x:x, y:y};
	},
	getSize : function(e)
	{
		var w = jQuery.css(e,'width');
		var h = jQuery.css(e,'height');
		var wb = 0;
		var hb = 0;
		es = e.style;
		if (jQuery(e).css('display') != 'none') {
			wb = e.offsetWidth;
			hb = e.offsetHeight;
		} else {
			oldVisibility = es.visibility;
			oldPosition = es.position;
			es.visibility = 'hidden';
			es.display = 'block';
			es.position = 'absolute';
			wb = e.offsetWidth;
			hb = e.offsetHeight;
			es.display = 'none';
			es.position = oldPosition;
			es.visibility = oldVisibility;
		}
		return {w:w, h:h, wb:wb, hb:hb};
	},
	getClient : function(e)
	{
		if (e) {
			w = e.clientWidth;
			h = e.clientHeight;
		} else {
			de = document.documentElement;
			w = window.innerWidth || self.innerWidth || (de&&de.clientWidth) || document.body.clientWidth;
			h = window.innerHeight || self.innerHeight || (de&&de.clientHeight) || document.body.clientHeight;
		}
		return {w:w,h:h};
	},
	getScroll : function (e) 
	{
		if (e) {
			t = e.scrollTop;
			l = e.scrollLeft;
			w = e.scrollWidth;
			h = e.scrollHeight;
			iw = 0;
			ih = 0;
		} else  {
			if (document.documentElement && document.documentElement.scrollTop) {
				t = document.documentElement.scrollTop;
				l = document.documentElement.scrollLeft;
				w = document.documentElement.scrollWidth;
				h = document.documentElement.scrollHeight;
			} else if (document.body) {
				t = document.body.scrollTop;
				l = document.body.scrollLeft;
				w = document.body.scrollWidth;
				h = document.body.scrollHeight;
			}
			iw = self.innerWidth||document.documentElement.clientWidth||document.body.clientWidth||0;
			ih = self.innerHeight||document.documentElement.clientHeight||document.body.clientHeight||0;
		}
		return { t: t, l: l, w: w, h: h, iw: iw, ih: ih };
	},
	getMargins : function(e, toInteger)
	{
		el = jQuery(e);
		t = el.css('marginTop') || '';
		r = el.css('marginRight') || '';
		b = el.css('marginBottom') || '';
		l = el.css('marginLeft') || '';
		if (toInteger)
			return {
				t: parseInt(t)||0, 
				r: parseInt(r)||0,
				b: parseInt(b)||0,
				l: parseInt(l)
			};
		else
			return {t: t, r: r,	b: b, l: l};
	},
	getPadding : function(e, toInteger)
	{
		el = jQuery(e);
		t = el.css('paddingTop') || '';
		r = el.css('paddingRight') || '';
		b = el.css('paddingBottom') || '';
		l = el.css('paddingLeft') || '';
		if (toInteger)
			return {
				t: parseInt(t)||0, 
				r: parseInt(r)||0,
				b: parseInt(b)||0,
				l: parseInt(l)
			};
		else
			return {t: t, r: r,	b: b, l: l};
	},
	getBorder : function(e, toInteger)
	{
		el = jQuery(e);
		t = el.css('borderTopWidth') || '';
		r = el.css('borderRightWidth') || '';
		b = el.css('borderBottomWidth') || '';
		l = el.css('borderLeftWidth') || '';
		if (toInteger)
			return {
				t: parseInt(t)||0, 
				r: parseInt(r)||0,
				b: parseInt(b)||0,
				l: parseInt(l)||0
			};
		else
			return {t: t, r: r,	b: b, l: l};
	},
	getPointer : function(event)
	{
		x = event.pageX || (event.clientX + (document.documentElement.scrollLeft || document.body.scrollLeft)) || 0;
		y = event.pageY || (event.clientY + (document.documentElement.scrollTop || document.body.scrollTop)) || 0;
		return {x:x, y:y};	
	}
};
