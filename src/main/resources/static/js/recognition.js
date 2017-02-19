var NONE = new DebugLevel(0, "NONE");
var ERROR = new DebugLevel(1, "ERROR");
var WARN = new DebugLevel(2, "WARN");
var INFORMATIONAL = new DebugLevel(3, "INFORMATIONAL");
var DEBUG = new DebugLevel(4, "DEBUG");
var TRACE = new DebugLevel(5, "TRACE");
var whitespace = "\n\n\t ";

// Mock Alfresco Types
// wenn 'space' nicht vorhanden ist, dann laufen wir wohl nicht im Alfresco
if (typeof (space) == "undefined") {

    function BasicObject(name) {

        this.generateUUID = function () {
            var d = new Date().getTime();
            return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
                var r = (d + Math.random()*16)%16 | 0;
                d = Math.floor(d/16);
                return (c=='x' ? r : (r&0x3|0x8)).toString(16);
            });
        };

        this.equals = function(obj) {
            return obj.id == this.id;
        };

        this.id = this.generateUUID();
        this.name = name;

    }

    function BasicNode(name) {
        BasicObject.call(this, name);
        this.aspect = new Liste();
    }

    BasicNode.prototype = new BasicObject();
    BasicNode.prototype.constructor = BasicNode;
    BasicNode.prototype.addAspect = function (aspect) {
        if (!this.hasAspect(aspect))
            this.aspect.add(new BasicObject(aspect));
    };
    BasicNode.prototype.hasAspect = function (aspect) {
        return this.aspect.contains(new BasicObject(aspect))
    };

    function Liste(){
        Array.call(this);
    }
    Liste.prototype = [];
    Liste.prototype.contains = function(element) {
        for (var key in this) {
            if (this.hasOwnProperty(key)) {
                if (this[key].name == element.name)
                    return true;
            }
        }
        return false;
    };
    Liste.prototype.add = function(element) {
        if (this.contains(element))
            throw "Element " + element.name + " bereits vorhanden";
        this.push(element);
    };
    Liste.prototype.remove = function(element) {
        if (!this.contains(element))
            throw "Element " + element.name + " nicht vorhanden";
        for (var i = 0; i < this.length; i++) {
            if (this[i] == element)
                delete this[i];
        }
    };
    Liste.prototype.get = function(element){
        for (var key in this) {
            if (this.hasOwnProperty(key)) {
                if (this[key].name == element.name)
                    return this[key];
            }
        }
        return null;
    };
    Liste.prototype.clear = function() {
        for (var key in this) {
            if (this.hasOwnProperty(key)) {
                delete this[key];
            }
        }
        this.length = 0;
    };
    Liste.prototype.clone = function() {
        var newList = new Liste();
        for (var key in this) {
            if (this.hasOwnProperty(key)) {
                if (typeof this[key] == "object" && "clone" in this[key])
                    newList[key] = this[key].clone();
                else
                    newList[key] = this[key];
            }
        }
        return newList;
    };


    function Content(cont) {
        BasicObject.call(this);
        if (typeof cont == "string")
            this.content = cont;
        else
            this.content = "";
    }

    Content.prototype = new BasicObject();
    Content.prototype.constructor = Content;

    Content.prototype.write = function(cont) {
        this.content = cont.content;
    };
    Content.prototype.clone = function() {
       var newContent = new Content();
        newContent.content = this.content;
        return newContent;
    };

    function ScriptNode(name, type) {
        BasicNode.call(this, name);
        this.subType = "";
        this.tags = new Liste();
        this.properties = new Liste();
        this.properties["content"] = new Content();
        this.children = new Liste();
        this.childAssocs = new Liste();
        this.parent = new Liste();
        this.versions = new Liste();
        this.type = type;
        this.workingParent = null;
        this.displayPath = "";

        this._getDisplayPath = function () {
            var path = [];
            var parent = null;
            if (this.parent.length > 0)
                parent = this.parent[0];
            while (parent != null) {
                path.unshift(parent.name);
                if (parent.parent.length > 0)
                    parent = parent.parent[0];
                else
                    parent = null;
            }
            return "/" + path.join("/");
        };
        return this;
    }
    ScriptNode.prototype = new BasicNode();
    ScriptNode.prototype.constructor = ScriptNode;

    ScriptNode.prototype.childByNamePath = function (name) {
        var parts = name.split("/");
        var currentNode = this;
        for (var i = 0; i < parts.length; i++) {
            var part = new BasicObject(parts[i]);
            if (part.name.length > 0) {
                if (!currentNode.children.contains(part))
                    break;
                else
                    currentNode = currentNode.children.get(part);
            }
            if (i == parts.length - 1)
                return currentNode;
        }
        return null;
    };

    ScriptNode.prototype.createAssociation = function(target, name){
        var asoc;
        if (this.childAssocs[name] != null){
            asoc = this.childAssocs[name];
            asoc.push(target);
        } else {
            asoc = new Liste();
            asoc.push(target);
            this.childAssocs[name] = asoc;
        }
    };

    ScriptNode.prototype.createFolder = function (name) {
        if (this.type != "cm:folder")
            throw "Kein Folder!";
        var newFolder = new ScriptNode(name, "cm:folder");
        this.children.add(newFolder);
        newFolder.parent.add(this);
        newFolder.displayPath = newFolder._getDisplayPath();
        return newFolder;
    };

    ScriptNode.prototype.isSubType = function (type) {
        return this.subType == type;
    };

    ScriptNode.prototype.addTag = function (tag) {
        if (!this.hasTag(tag))
            this.tags.add(new BasicObject(tag));
    };

    ScriptNode.prototype.hasTag = function(tag) {
        return this.tags.contains(new BasicObject(tag))

    };

    ScriptNode.prototype.checkout = function () {
        if (this.hasAspect(("cm:workingcopy")))
            throw "Der Knoten " + this.name + " ist bereits ausgecheckt!";
        var workNode = this.clone();
        workNode.addAspect("cm:workingcopy");
        workNode.workingParent = this;
        return workNode;
    };

    ScriptNode.prototype.checkoutForUpload = function() {
       return this.checkout();
    };

    ScriptNode.prototype.checkin = function () {
        if (!REC.exist(this.workingParent) || !this.hasAspect(("cm:workingcopy")))
            throw "Der Knoten " + this.name + " ist nicht ausgecheckt!";
        this.aspect.remove(new BasicObject("cm:workingcopy"));
        var i = 1;
        while(this.workingParent.versions.contains(new BasicObject(i)))
            i++;
        this.workingParent.properties = this.properties;
        var version = new BasicObject(i);
        version.value = this.workingParent;
        this.workingParent.versions.add(version);
        return this.workingParent;
    };

    ScriptNode.prototype.getVersion = function(label) {
        if (!this.isVersioned())
            throw this.name + " ist nicht versioniert!";
        if (!this.versions.contains(new BasicObject(label)))
            throw "Version " + label + " von " +this.name + " ist nicht vorhanden!";
        return this.versions.get(new BasicObject(label)).value;
    };

    ScriptNode.prototype.isVersioned = function() {
        return this.versions.length > 0;
    };

    ScriptNode.prototype.specializeType = function (type) {
        this.subType = type;
    };

    ScriptNode.prototype.createNode = function (name, typ, assocType) {
        if (this.type != "cm:folder" && this.type != "fm:forum" && this.type != "fm:topic")
            throw "Kein Folder!";
        var newNode =  new ScriptNode(name, typ);
        this.children.add(newNode);
        newNode.parent.add(this);
        newNode.displayPath = newNode._getDisplayPath();
        if (typeof assocType != "undefined")
            this.createAssociation(newNode, assocType);
        return newNode;
    };

    ScriptNode.prototype.addNode = function(node){
        if (this.type != "cm:folder" && this.type != "fm:forum" && this.type != "fm:topic")
            throw "Kein Folder!";
        this.children.add(node);
        node.parent.add(this);
        node.displayPath = node._getDisplayPath();
    };

    ScriptNode.prototype.save = function () {
    };

    ScriptNode.prototype.remove = function () {
        for (var i = 0; i < this.parent.length; i++) {
            this.parent[i].children.remove(this);
            for (assoc in this.parent[i].childAssocs) {
                if (typeof this.parent[i].childAssocs[assoc] == "object" && this.parent[i].childAssocs[assoc][0] == this)
                    delete this.parent[i].childAssocs[assoc];

            }
        }
        return true;
    };

    ScriptNode.prototype.copy = function(newNode) {
        var cNode =  new ScriptNode(this.name, this.typ);
        newNode.children.add(cNode);
        cNode.parent.add(newNode);
        return cNode;
    };

    ScriptNode.prototype.transformDocument = function (mimeType) {
        return this.clone();
    };

    ScriptNode.prototype.move = function (newNode) {
        for (var i = 0; i < this.parent.length; i++) {
            this.parent[i].children.remove(this);
        }
        newNode.children.add(this);
        this.parent.clear();
        this.parent.add(newNode);
        newNode.displayPath = newNode._getDisplayPath();
        return true;
    };

    ScriptNode.prototype.setProperty = function(key, value){
        this.properties[key] = value;
    };

    ScriptNode.prototype.ensureVersioningEnabled = function(autoVersion, autoVersionProps){
        if (!this.hasAspect("cm:versionable")) {
            this.addAspect("cm:versionable");
            var version = new BasicObject(1);
            version.value = this.clone();
            this.versions.add(version);
        }
    };

    ScriptNode.prototype.clone = function(){
        var newNode = new ScriptNode(this.name, this.typ);
        newNode.subType = this.subType;
        newNode.aspect = this.aspect.clone();
        newNode.tags = this.tags.clone();
        newNode.properties = this.properties.clone();
        newNode.content = newNode.properties["content"].content;
        newNode.children = this.children;
        newNode.versions = this.versions;
        newNode.parent = this.parent;
        newNode.displayPath = this.displayPath;
        return newNode;
    };

    ScriptNode.prototype.init = function() {
        this.subType = "";
        this.aspect.clear();
        this.tags.clear();
        this.properties.clear();
        this.properties["content"] = new Content();
        this.content = this.properties["content"].content;
        this.children.clear();
        this.childAssocs.clear();
        this.versions.clear();
        this.parent.clear();
        this.workingParent = null;
        this.displayPath = "";
    };
    var companyhome = new ScriptNode("/", "cm:folder");

    function CommentService () {
        this.createCommentsFolder= function (node) {
            var discussion =  new ScriptNode(node.name + " Comments", "fm:forum");
            node.createAssociation(discussion, "fm:discussion");
            node.addAspect("fm:discussable");
            var commentsNode = new ScriptNode("Comments", "fm:topic");
            discussion.addNode(commentsNode);
            return commentsNode;
        };
    }

    var commentService = new CommentService();

    function CategoryNode(aspect, name) {
        BasicNode.call(this, name);
        this.addAspect(aspect);
        this.isCategory = true;
        this.categoryMembers = [];
        this.rootCategories = new Liste();
        this.subCategories = new Liste();
        this.membersAndSubCategories = [];
        this.immediateCategoryMembers = [];
        this.immediateSubCategories = [];
        this.immediateMembersAndSubCategories = [];
    }

    CategoryNode.prototype = new BasicNode();
    CategoryNode.prototype.constructor = CategoryNode;

    CategoryNode.prototype.init = function() {
        this.isCategory = true;
        this.aspect.clear();
        this.categoryMembers = [];
        this.rootCategories.clear();
        this.subCategories.clear();
        this.membersAndSubCategories = [];
        this.immediateCategoryMembers = [];
        this.immediateSubCategories = [];
        this.immediateMembersAndSubCategories = [];
    };

    CategoryNode.prototype.createSubCategory = function(name) {
        var category = new CategoryNode(this.aspect[0], name);
        this.subCategories.add(category);
        return category;
    };

    CategoryNode.prototype.createRootCategory = function(aspect, name) {
        var obj = new BasicObject(name);
        if (this.rootCategories.contains(obj))
            throw "Root Category " + name + " bereits vorhanden!";
        var rootCategory =  new CategoryNode(aspect, name);
        this.rootCategories.add(rootCategory);
        return rootCategory;
    };

    CategoryNode.prototype.getRootCategories = function(aspect) {
        var result = [];
        for (var key in this.rootCategories) {
            if (key != "length" && this.rootCategories.hasOwnProperty(key)) {
                if (this.rootCategories[key].hasAspect(aspect))
                    result.push(this.rootCategories[key]);
            }
        }
        return result;
    };

    CategoryNode.prototype.remove = function() {
    };

    var classification = new CategoryNode("cm:generalclassifiable", "classification");
    classification.init();

    var search = ({
        willFind: false,
        node: null,
        setFind: function(value, node) {this.willFind = value;
            this.node = node;
        },
        luceneSearch: function (xPath) {
            if (!this.willFind)
                return [];
            else
                return [this.node];
        }
    });
}

Encoder = {

    // When encoding do we convert characters into html or numerical entities
    EncodeType : "entity",  // entity OR numerical

    isEmpty : function(val){
        if(val){
            return ((val===null) || val.length==0 || /^\s+$/.test(val));
        }else{
            return true;
        }
    },

    // arrays for conversion from HTML Entities to Numerical values
    arr1: ['&nbsp;','&iexcl;','&cent;','&pound;','&curren;','&yen;','&brvbar;','&sect;','&uml;','&copy;','&ordf;','&laquo;','&not;','&shy;','&reg;','&macr;','&deg;','&plusmn;','&sup2;','&sup3;','&acute;','&micro;','&para;','&middot;','&cedil;','&sup1;','&ordm;','&raquo;','&frac14;','&frac12;','&frac34;','&iquest;','&Agrave;','&Aacute;','&Acirc;','&Atilde;','&Auml;','&Aring;','&AElig;','&Ccedil;','&Egrave;','&Eacute;','&Ecirc;','&Euml;','&Igrave;','&Iacute;','&Icirc;','&Iuml;','&ETH;','&Ntilde;','&Ograve;','&Oacute;','&Ocirc;','&Otilde;','&Ouml;','&times;','&Oslash;','&Ugrave;','&Uacute;','&Ucirc;','&Uuml;','&Yacute;','&THORN;','&szlig;','&agrave;','&aacute;','&acirc;','&atilde;','&auml;','&aring;','&aelig;','&ccedil;','&egrave;','&eacute;','&ecirc;','&euml;','&igrave;','&iacute;','&icirc;','&iuml;','&eth;','&ntilde;','&ograve;','&oacute;','&ocirc;','&otilde;','&ouml;','&divide;','&oslash;','&ugrave;','&uacute;','&ucirc;','&uuml;','&yacute;','&thorn;','&yuml;','&quot;','&amp;','&lt;','&gt;','&OElig;','&oelig;','&Scaron;','&scaron;','&Yuml;','&circ;','&tilde;','&ensp;','&emsp;','&thinsp;','&zwnj;','&zwj;','&lrm;','&rlm;','&ndash;','&mdash;','&lsquo;','&rsquo;','&sbquo;','&ldquo;','&rdquo;','&bdquo;','&dagger;','&Dagger;','&permil;','&lsaquo;','&rsaquo;','&euro;','&fnof;','&Alpha;','&Beta;','&Gamma;','&Delta;','&Epsilon;','&Zeta;','&Eta;','&Theta;','&Iota;','&Kappa;','&Lambda;','&Mu;','&Nu;','&Xi;','&Omicron;','&Pi;','&Rho;','&Sigma;','&Tau;','&Upsilon;','&Phi;','&Chi;','&Psi;','&Omega;','&alpha;','&beta;','&gamma;','&delta;','&epsilon;','&zeta;','&eta;','&theta;','&iota;','&kappa;','&lambda;','&mu;','&nu;','&xi;','&omicron;','&pi;','&rho;','&sigmaf;','&sigma;','&tau;','&upsilon;','&phi;','&chi;','&psi;','&omega;','&thetasym;','&upsih;','&piv;','&bull;','&hellip;','&prime;','&Prime;','&oline;','&frasl;','&weierp;','&image;','&real;','&trade;','&alefsym;','&larr;','&uarr;','&rarr;','&darr;','&harr;','&crarr;','&lArr;','&uArr;','&rArr;','&dArr;','&hArr;','&forall;','&part;','&exist;','&empty;','&nabla;','&isin;','&notin;','&ni;','&prod;','&sum;','&minus;','&lowast;','&radic;','&prop;','&infin;','&ang;','&and;','&or;','&cap;','&cup;','&int;','&there4;','&sim;','&cong;','&asymp;','&ne;','&equiv;','&le;','&ge;','&sub;','&sup;','&nsub;','&sube;','&supe;','&oplus;','&otimes;','&perp;','&sdot;','&lceil;','&rceil;','&lfloor;','&rfloor;','&lang;','&rang;','&loz;','&spades;','&clubs;','&hearts;','&diams;'],
    arr2: ['&#160;','&#161;','&#162;','&#163;','&#164;','&#165;','&#166;','&#167;','&#168;','&#169;','&#170;','&#171;','&#172;','&#173;','&#174;','&#175;','&#176;','&#177;','&#178;','&#179;','&#180;','&#181;','&#182;','&#183;','&#184;','&#185;','&#186;','&#187;','&#188;','&#189;','&#190;','&#191;','&#192;','&#193;','&#194;','&#195;','&#196;','&#197;','&#198;','&#199;','&#200;','&#201;','&#202;','&#203;','&#204;','&#205;','&#206;','&#207;','&#208;','&#209;','&#210;','&#211;','&#212;','&#213;','&#214;','&#215;','&#216;','&#217;','&#218;','&#219;','&#220;','&#221;','&#222;','&#223;','&#224;','&#225;','&#226;','&#227;','&#228;','&#229;','&#230;','&#231;','&#232;','&#233;','&#234;','&#235;','&#236;','&#237;','&#238;','&#239;','&#240;','&#241;','&#242;','&#243;','&#244;','&#245;','&#246;','&#247;','&#248;','&#249;','&#250;','&#251;','&#252;','&#253;','&#254;','&#255;','&#34;','&#38;','&#60;','&#62;','&#338;','&#339;','&#352;','&#353;','&#376;','&#710;','&#732;','&#8194;','&#8195;','&#8201;','&#8204;','&#8205;','&#8206;','&#8207;','&#8211;','&#8212;','&#8216;','&#8217;','&#8218;','&#8220;','&#8221;','&#8222;','&#8224;','&#8225;','&#8240;','&#8249;','&#8250;','&#8364;','&#402;','&#913;','&#914;','&#915;','&#916;','&#917;','&#918;','&#919;','&#920;','&#921;','&#922;','&#923;','&#924;','&#925;','&#926;','&#927;','&#928;','&#929;','&#931;','&#932;','&#933;','&#934;','&#935;','&#936;','&#937;','&#945;','&#946;','&#947;','&#948;','&#949;','&#950;','&#951;','&#952;','&#953;','&#954;','&#955;','&#956;','&#957;','&#958;','&#959;','&#960;','&#961;','&#962;','&#963;','&#964;','&#965;','&#966;','&#967;','&#968;','&#969;','&#977;','&#978;','&#982;','&#8226;','&#8230;','&#8242;','&#8243;','&#8254;','&#8260;','&#8472;','&#8465;','&#8476;','&#8482;','&#8501;','&#8592;','&#8593;','&#8594;','&#8595;','&#8596;','&#8629;','&#8656;','&#8657;','&#8658;','&#8659;','&#8660;','&#8704;','&#8706;','&#8707;','&#8709;','&#8711;','&#8712;','&#8713;','&#8715;','&#8719;','&#8721;','&#8722;','&#8727;','&#8730;','&#8733;','&#8734;','&#8736;','&#8743;','&#8744;','&#8745;','&#8746;','&#8747;','&#8756;','&#8764;','&#8773;','&#8776;','&#8800;','&#8801;','&#8804;','&#8805;','&#8834;','&#8835;','&#8836;','&#8838;','&#8839;','&#8853;','&#8855;','&#8869;','&#8901;','&#8968;','&#8969;','&#8970;','&#8971;','&#9001;','&#9002;','&#9674;','&#9824;','&#9827;','&#9829;','&#9830;'],

    // Convert HTML entities into numerical entities
    HTML2Numerical : function(s){
        return this.swapArrayVals(s,this.arr1,this.arr2);
    },

    // Convert Numerical entities into HTML entities
    NumericalToHTML : function(s){
        return this.swapArrayVals(s,this.arr2,this.arr1);
    },


    // Numerically encodes all unicode characters
    numEncode : function(s){
        if(this.isEmpty(s)) return "";

        var a = [],
            l = s.length;

        for (var i=0;i<l;i++){
            var c = s.charAt(i);
            if (c < " " || c > "~"){
                a.push("&#");
                a.push(c.charCodeAt()); //numeric value of code point
                a.push(";");
            }else{
                a.push(c);
            }
        }

        return a.join("");
    },

    // HTML Decode numerical and HTML entities back to original values
    htmlDecode : function(s){

        var c,m,d = s;

        if(this.isEmpty(d)) return "";

        // convert HTML entites back to numerical entites first
        d = this.HTML2Numerical(d);

        // look for numerical entities &#34;
        var arr = d.match(/&#[0-9]{1,5};/g);

        // if no matches found in string then skip
        if(arr!=null){
            for(var x=0;x<arr.length;x++){
                m = arr[x];
                c = m.substring(2,m.length-1); //get numeric part which is refernce to unicode character
                // if its a valid number we can decode
                if(c >= -32768 && c <= 65535){
                    // decode every single match within string
                    d = d.replace(m, String.fromCharCode(c));
                }else{
                    d = d.replace(m, ""); //invalid so replace with nada
                }
            }
        }

        return d;
    },

    // encode an input string into either numerical or HTML entities
    htmlEncode : function(s,dbl){

        if(this.isEmpty(s)) return "";

        // do we allow double encoding? E.g will &amp; be turned into &amp;amp;
        dbl = dbl || false; //default to prevent double encoding

        // if allowing double encoding we do ampersands first
        if(dbl){
            if(this.EncodeType=="numerical"){
                s = s.replace(/&/g, "&#38;");
            }else{
                s = s.replace(/&/g, "&amp;");
            }
        }

        // convert the xss chars to numerical entities ' " < >
        s = this.XSSEncode(s,false);

        if(this.EncodeType=="numerical" || !dbl){
            // Now call function that will convert any HTML entities to numerical codes
            s = this.HTML2Numerical(s);
        }

        // Now encode all chars above 127 e.g unicode
        s = this.numEncode(s);

        // now we know anything that needs to be encoded has been converted to numerical entities we
        // can encode any ampersands & that are not part of encoded entities
        // to handle the fact that I need to do a negative check and handle multiple ampersands &&&
        // I am going to use a placeholder

        // if we don't want double encoded entities we ignore the & in existing entities
        if(!dbl){
            s = s.replace(/&#/g,"##AMPHASH##");

            if(this.EncodeType=="numerical"){
                s = s.replace(/&/g, "&#38;");
            }else{
                s = s.replace(/&/g, "&amp;");
            }

            s = s.replace(/##AMPHASH##/g,"&#");
        }

        // replace any malformed entities
        s = s.replace(/&#\d*([^\d;]|$)/g, "$1");

        if(!dbl){
            // safety check to correct any double encoded &amp;
            s = this.correctEncoding(s);
        }

        // now do we need to convert our numerical encoded string into entities
        if(this.EncodeType=="entity"){
            s = this.NumericalToHTML(s);
        }

        return s;
    },

    // Encodes the basic 4 characters used to malform HTML in XSS hacks
    XSSEncode : function(s,en){
        if(!this.isEmpty(s)){
            en = en || true;
            // do we convert to numerical or html entity?
            if(en){
                s = s.replace(/\'/g,"&#39;"); //no HTML equivalent as &apos is not cross browser supported
                s = s.replace(/\"/g,"&quot;");
                s = s.replace(/</g,"&lt;");
                s = s.replace(/>/g,"&gt;");
            }else{
                s = s.replace(/\'/g,"&#39;"); //no HTML equivalent as &apos is not cross browser supported
                s = s.replace(/\"/g,"&#34;");
                s = s.replace(/</g,"&#60;");
                s = s.replace(/>/g,"&#62;");
            }
            return s;
        }else{
            return "";
        }
    },

    // returns true if a string contains html or numerical encoded entities
    hasEncoded : function(s){
        if(/&#[0-9]{1,5};/g.test(s)){
            return true;
        }else if(/&[A-Z]{2,6};/gi.test(s)){
            return true;
        }else{
            return false;
        }
    },

    // will remove any unicode characters
    stripUnicode : function(s){
        return s.replace(/[^\x20-\x7E]/g,"");

    },

    // corrects any double encoded &amp; entities e.g &amp;amp;
    correctEncoding : function(s){
        return s.replace(/(&amp;)(amp;)+/,"$1");
    },


    // Function to loop through an array swaping each item with the value from another array e.g swap HTML entities with Numericals
    swapArrayVals : function(s,arr1,arr2){
        if(this.isEmpty(s)) return "";
        var re;
        if(arr1 && arr2){
            //ShowDebug("in swapArrayVals arr1.length = " + arr1.length + " arr2.length = " + arr2.length)
            // array lengths must match
            if(arr1.length == arr2.length){
                for(var x=0,i=arr1.length;x<i;x++){
                    re = new RegExp(arr1[x], 'g');
                    s = s.replace(re,arr2[x]); //swap arr1 item with matching item from arr2
                }
            }
        }
        return s;
    },

    inArray : function( item, arr ) {
        for ( var i = 0, x = arr.length; i < x; i++ ){
            if ( arr[i] === item ){
                return i;
            }
        }
        return -1;
    }

}

XMLDoc = {
    quotes: "\"'",
    constructor: function (source, errFn) {
        this.topNode = null;
        // set up the properties and methods for this object
        this.errFn = errFn; // user defined error functions
        this.hasErrors = false; // were errors found during the parse?
        this.source = source; // the string source of the document
    },

    createXMLNode: function (strXML) {
        return new XMLDoc(strXML, this.errFn).docNode;
    },

    error: function (str) {
        this.hasErrors = true;
        if (this.errFn) {
            this.errFn("ERROR: " + str);
        } else if (this.onerror) {
            this.onerror("ERROR: " + str);
        }
        return 0;
    },

    getTagNameParams: function (tag, obj) {
        var elm = -1, e, s = tag.indexOf('[');
        var attr = [];
        if (s >= 0) {
            e = tag.indexOf(']');
            if (e >= 0)
                elm = tag.substr(s + 1, (e - s) - 1);
            else
                obj.error('expected ] near ' + tag);
            tag = tag.substr(0, s);
            if (isNaN(elm) && elm != '*') {
                attr = elm.substr(1, elm.length - 1); // remove @
                attr = attr.split('=');
                if (attr[1]) { // remove "
                    s = attr[1].indexOf('"');
                    attr[1] = attr[1].substr(s + 1, attr[1].length - 1);
                    e = attr[1].indexOf('"');
                    if (e >= 0)
                        attr[1] = attr[1].substr(0, e);
                    else
                        obj.error('expected " near ' + tag);
                }
                elm = -1;
            } else if (elm == '*')
                elm = -1;
        }
        return [tag, elm, attr[0], attr[1]];
    },

    getUnderlyingXMLText: function () {
        var strRet = "";
        // for now, hardcode the xml version 1 information. When we handle Processing
        // Instructions later, this
        // should be looked at again
        strRet = strRet + "<?xml version=\"1.0\"?>";
        if (this.docNode == null) {
            return;
        }
        strRet = REC.displayElement(this.docNode, strRet);
        return strRet;
    },

    handleNode: function (current) {
        if ((current.nodeType == 'COMMENT') && (this.topNode != null)) {
            return this.topNode.addElement(current);
        } else if ((current.nodeType == 'TEXT') || (current.nodeType == 'CDATA')) {
            // if the current node is a text node:
            // if the stack is empty, and this text node isn't just whitespace, we have
            // a problem (we're not in a document element)
            if (this.topNode == null) {
                if (REC.trim(current.content, true, false) == "") {
                    return true;
                } else {
                    return this.error("expected document node, found: " + current);
                }
            } else {
                // otherwise, append this as child to the element at the top of the stack
                return this.topNode.addElement(current);
            }
        } else if ((current.nodeType == 'OPEN') || (current.nodeType == 'SINGLE')) {
            // if we find an element tag (open or empty)
            var success = false;
            // if the stack is empty, this node becomes the document node
            if (this.topNode == null) {
                this.docNode = current;
                current.parent = null;
                success = true;
            } else {
                // otherwise, append this as child to the element at the top of the stack
                success = this.topNode.addElement(current);
            }

            if (success && (current.nodeType != 'SINGLE')) {
                this.topNode = current;
            }
            // rename it as an element node
            current.nodeType = "ELEMENT";
            return success;
        }
        // if it's a close tag, check the nesting
        else if (current.nodeType == 'CLOSE') {
            // if the stack is empty, it's certainly an error
            if (this.topNode == null) {
                return this.error("close tag without open: " + current.toString());
            } else {
                // otherwise, check that this node matches the one on the top of the stack
                if (current.tagName != this.topNode.tagName) {
                    return this.error("expected closing " + this.topNode.tagName + ", found closing " + current.tagName);
                } else {
                    // if it does, pop the element off the top of the stack
                    this.topNode = this.topNode.getParent();
                }
            }
        }
        return true;
    },

    insertNodeAfter: function (referenceNode, newNode) {
        var parentXMLText = this.getUnderlyingXMLText();
        var selectedNodeXMLText = referenceNode.getUnderlyingXMLText();
        var originalNodePos = parentXMLText.indexOf(selectedNodeXMLText) + selectedNodeXMLText.length;
        var newXML = parentXMLText.substr(0, originalNodePos);
        newXML += newNode.getUnderlyingXMLText();
        newXML += parentXMLText.substr(originalNodePos);
        var newDoc = new XMLDoc(newXML, this.errFn);
        return newDoc;
    },

    insertNodeInto: function (referenceNode, insertNode) {
        var parentXMLText = this.getUnderlyingXMLText();
        var selectedNodeXMLText = referenceNode.getUnderlyingXMLText();
        var endFirstTag = selectedNodeXMLText.indexOf(">") + 1;
        var originalNodePos = parentXMLText.indexOf(selectedNodeXMLText) + endFirstTag;
        var newXML = parentXMLText.substr(0, originalNodePos);
        newXML += insertNode.getUnderlyingXMLText();
        newXML += parentXMLText.substr(originalNodePos);
        var newDoc = new XMLDoc(newXML, this.errFn);
        return newDoc;
    },

    loadXML: function (sourceXML) {
        this.topNode = null;
        this.hasErrors = false;
        this.source = sourceXML;
    },


    parse: function () {
        var pos = 0;
        // set up the arrays used to store positions of < and > characters
        var err = false;
        while (!err) {
            var closing_tag_prefix = '';
            var chpos = this.source.indexOf('<', pos);
            var open;
            var close;
            if (chpos == -1) {
                break;
            }
            open = chpos;
            // create a text node
            var str = this.source.substring(pos, open);
            if (str.length != 0) {
                err = !this.handleNode(new XMLNode('TEXT', this, str));
            }
            // handle Programming Instructions - they can't reliably be handled as tags
            if (chpos == this.source.indexOf("<?", pos)) {
                pos = this.parsePI(this.source, pos + 2);
                if (pos == 0) {
                    err = true;
                }
                continue;
            }
            // nobble the document type definition
            if (chpos == this.source.indexOf("<!DOCTYPE", pos)) {
                pos = this.parseDTD(this.source, chpos + 9);
                if (pos == 0) {
                    err = true;
                }
                continue;
            }
            // if we found an open comment, we need to ignore angle brackets
            // until we find a close comment
            if (chpos == this.source.indexOf('<!--', pos)) {
                var open_length = 4;
                closing_tag_prefix = '--';
            }
            // similarly, if we find an open CDATA, we need to ignore all angle
            // brackets until a close CDATA sequence is found
            if (chpos == this.source.indexOf('<![CDATA[', pos)) {
                var open_length = 9;
                closing_tag_prefix = ']]';
            }
            // look for the closing sequence
            chpos = this.source.indexOf(closing_tag_prefix + '>', chpos);
            if (chpos == -1) {
                return this.error("expected closing tag sequence: " + closing_tag_prefix + '>');
            }
            close = chpos + closing_tag_prefix.length;
            // create a tag node
            str = this.source.substring(open + 1, close);
            var n = this.parseTag(str);
            if (n) {
                err = !this.handleNode(n);
            }
            pos = close + 1;
            // and loop
        }
        return !err;
    },


    parseAttribute: function (src, pos, node) {
        // chew up the whitespace, if any
        while ((pos < src.length) && (whitespace.indexOf(src.charAt(pos)) != -1)) {
            pos++;
        }
        // if there's nothing else, we have no (more) attributes - just break out
        if (pos >= src.length) {
            return pos;
        }
        var p1 = pos;
        while ((pos < src.length) && (src.charAt(pos) != '=')) {
            pos++;
        }
        var msg = "attributes must have values";
        // parameters without values aren't allowed.
        if (pos >= src.length) {
            return this.error(msg);
        }
        // extract the parameter name
        var paramname = REC.trim(src.substring(p1, pos++), false, true);
        // chew up whitespace
        while ((pos < src.length) && (whitespace.indexOf(src.charAt(pos)) != -1)) {
            pos++;
        }
        // throw an error if we've run out of string
        if (pos >= src.length) {
            return this.error(msg);
        }
        msg = "attribute values must be in quotes";
        // check for a quote mark to identify the beginning of the attribute value
        var quote = src.charAt(pos++);
        // throw an error if we didn't find one
        if (this.quotes.indexOf(quote) == -1) {
            return this.error(msg);
        }
        p1 = pos;
        while ((pos < src.length) && (src.charAt(pos) != quote)) {
            pos++;
        }
        // throw an error if we found no closing quote
        if (pos >= src.length) {
            return this.error(msg);
        }
        // store the parameter
        if (!node.addAttribute(paramname, REC.trim(src.substring(p1, pos++), false, true))) {
            return 0;
        }
        return pos;
    },

    parseDTD: function (str, pos) {
        // we're just going to discard the DTD
        var firstClose = str.indexOf('>', pos);
        if (firstClose == -1) {
            return this.error("error in DTD: expected '>'");
        }
        var closing_tag_prefix = '';
        var firstOpenSquare = str.indexOf('[', pos);
        if ((firstOpenSquare != -1) && (firstOpenSquare < firstClose)) {
            closing_tag_prefix = ']';
        }
        while (true) {
            var closepos = str.indexOf(closing_tag_prefix + '>', pos);
            if (closepos == -1) {
                return this.error("expected closing tag sequence: " + closing_tag_prefix + '>');
            }
            pos = closepos + closing_tag_prefix.length + 1;
            if (str.substring(closepos - 1, closepos + 2) != ']]>') {
                break;
            }
        }
        return pos;
    },

    parsePI: function (str, pos) {
        // we just swallow them up
        var closepos = str.indexOf('?>', pos);
        return closepos + 2;
    },

    parseTag: function (src) {
        // if it's a comment, strip off the packaging, mark it a comment node
        // and return it
        if (src.indexOf('!--') == 0) {
            return new XMLNode('COMMENT', this, src.substring(3, src.length - 2));
        }
        // if it's CDATA, do similar
        if (src.indexOf('![CDATA[') == 0) {
            return new XMLNode('CDATA', this, src.substring(8, src.length - 2));
        }
        var n = new XMLNode();
        n.doc = this;
        if (src.charAt(0) == '/') {
            n.nodeType = 'CLOSE';
            src = src.substring(1);
        } else {
            // otherwise it's an open tag (possibly an empty element)
            n.nodeType = 'OPEN';
        }
        // if the last character is a /, check it's not a CLOSE tag
        if (src.charAt(src.length - 1) == '/') {
            if (n.nodeType == 'CLOSE') {
                return this.error("singleton close tag");
            } else {
                n.nodeType = 'SINGLE';
            }
            // strip off the last character
            src = src.substring(0, src.length - 1);
        }
        // set up the properties as appropriate
        if (n.nodeType != 'CLOSE') {
            n.attributes = [];
        }
        if (n.nodeType == 'OPEN') {
            n.children = [];
        }
        // trim the whitespace off the remaining content
        src = REC.trim(src, true, true);
        // chuck out an error if there's nothing left
        if (src.length == 0) {
            return this.error("empty tag");
        }
        // scan forward until a space...
        var endOfName = REC.firstWhiteChar(src, 0);
        // if there is no space, this is just a name (e.g. (<tag>, <tag/> or </tag>
        if (endOfName == -1) {
            n.tagName = src;
            return n;
        }
        // otherwise, we should expect attributes - but store the tag name first
        n.tagName = src.substring(0, endOfName);
        // start from after the tag name
        var pos = endOfName;
        // now we loop:
        while (pos < src.length) {
            pos = this.parseAttribute(src, pos, n);
            if (this.pos == 0) {
                return null;
            }
            // and loop
        }
        return n;
    },

    removeNodeFromTree: function (node) {
        var parentXMLText = this.getUnderlyingXMLText();
        var selectedNodeXMLText = node.getUnderlyingXMLText();
        var originalNodePos = parentXMLText.indexOf(selectedNodeXMLText);
        var newXML = parentXMLText.substr(0, originalNodePos);
        newXML += parentXMLText.substr(originalNodePos + selectedNodeXMLText.length);
        var newDoc = new XMLDoc(newXML, this.errFn);
        return newDoc;
    },

    replaceNodeContents: function (referenceNode, newContents) {
        var newNode = this.createXMLNode("<X>" + newContents + "</X>");
        referenceNode.children = newNode.children;
        return this;
    },

    selectNode: function (tagpath) {
        tagpath = REC.trim(tagpath, true, true);
        var srcnode, node, tag, params, elm, rg;
        var tags, attrName, attrValue, ok;
        srcnode = node = ((this.source) ? this.docNode : this);
        if (!tagpath)
            return node;
        if (tagpath.indexOf('/') == 0)
            tagpath = tagpath.substr(1);
        tagpath = tagpath.replace(tag, '');
        tags = tagpath.split('/');
        tag = tags[0];
        if (tag) {
            if (tagpath.indexOf('/') == 0)
                tagpath = tagpath.substr(1);
            tagpath = tagpath.replace(tag, '');
            params = this.getTagNameParams(tag, this);
            tag = params[0];
            elm = params[1];
            attrName = params[2];
            attrValue = params[3];
            node = (tag == '*') ? node.getElements() : node.getElements(tag);
            if (node.length) {
                if (elm < 0) {
                    srcnode = node;
                    var i = 0;
                    while (i < srcnode.length) {
                        if (attrName) {
                            if (srcnode[i].getAttribute(attrName) != attrValue)
                                ok = false;
                            else
                                ok = true;
                        } else
                            ok = true;
                        if (ok) {
                            node = srcnode[i].selectNode(tagpath);
                            if (node)
                                return node;
                        }
                        i++;
                    }
                } else if (elm < node.length) {
                    node = node[elm].selectNode(tagpath);
                    if (node)
                        return node;
                }
            }
        }
    },

    selectNodeText: function (tagpath) {
        var node = this.selectNode(tagpath);
        if (node != null) {
            return node.getText();
        } else {
            return null;
        }
    }
}

function XMLNode(nodeType, doc, str) {
    // the content of text (also CDATA and COMMENT) nodes
    if (nodeType == 'TEXT' || nodeType == 'CDATA' || nodeType == 'COMMENT') {
        this.content = str;
    } else {
        this.content = null;
    }

    this.attributes = null; // an array of attributes (used as a hash table)
    this.children = null; // an array (list) of the children of this node
    this.doc = doc; // a reference to the document
    this.nodeType = nodeType; // the type of the node
    this.parent = "";
    this.tagName = ""; // the name of the tag (if a tag node)

    // configure the methods
    this.selectNode = XMLDoc.selectNode;
    this.selectNodeText = XMLDoc.selectNodeText;

     this.addAttribute = function(attributeName, attributeValue) {
        // if the name is found, the old value is overwritten by the new value
        this.attributes['_' + attributeName] = attributeValue;
        return true;
    };

    this.addElement = function(node) {
        node.parent = this;
        this.children[this.children.length] = node;
        return true;
    };

     this.getAttribute = function(name) {
        if (this.attributes == null) {
            return null;
        }
        return this.attributes['_' + name];
    };

    this.getAttributeNames = function() {
        if (this.attributes == null) {
            var ret = [];
            return ret;
        }
        var attlist = [];
        for (var a in this.attributes) {
            attlist[attlist.length] = a.substring(1);
        }
        return attlist;
    };

    this.getElementById = function(id) {
        var node = this;
        var ret;
        // alert("tag name=" + node.tagName);
        // alert("id=" + node.getAttribute("id"));
        if (node.getAttribute("id") == id) {
            return node;
        } else {
            var elements = node.getElements();
            // alert("length=" + rugrats.length);
            var intLoop = 0;
            // do NOT use a for loop here. For some reason
            // it kills some browsers!!!
            while (intLoop < elements.length) {
                // alert("intLoop=" + intLoop);
                var element = elements[intLoop];
                // alert("recursion");
                ret = element.getElementById(id);
                if (ret != null) {
                    // alert("breaking");
                    break;
                }
                intLoop++;
            }
        }
        return ret;
    };

    this.getElements = function(byName) {
        if (this.children == null) {
            var ret = [];
            return ret;
        }
        var elements = [];
        for (var i = 0; i < this.children.length; i++) {
            if ((this.children[i].nodeType == 'ELEMENT') && ((byName == null) || (this.children[i].tagName == byName))) {
                elements[elements.length] = this.children[i];
            }
        }
        return elements;
    };

    this.getText = function() {
        if (this.nodeType == 'ELEMENT') {
            if (this.children == null) {
                return null;
            }
            var str = "";
            for (var i = 0; i < this.children.length; i++) {
                var t = this.children[i].getText();
                str += (t == null ? "" : t);
            }
            return str;
        } else if (this.nodeType == 'TEXT') {
            return REC.convertEscapes(this.content);
        } else {
            return this.content;
        }
    };

    this.getParent = function() {
        return this.parent;
    };

    this.getUnderlyingXMLText = function() {
        var strRet = "";
        strRet = REC.displayElement(this, strRet);
        return strRet;
    };

    this.removeAttribute = function(attributeName) {
        if (attributeName == null) {
            return this.doc.error("You must pass an attribute name into the removeAttribute function");
        }
        // now remove the attribute from the list.
        // I want to keep the logic for adding attribtues in one place. I'm
        // going to get a temp array of attributes and values here and then
        // use the addAttribute function to re-add the attributes
        var attributes = this.getAttributeNames();
        var intCount = attributes.length;
        var tmpAttributeValues = [];
        for (var intLoop = 0; intLoop < intCount; intLoop++) {
            tmpAttributeValues[intLoop] = this.getAttribute(attributes[intLoop]);
        }
        // now blow away the old attribute list
        this.attributes = [];

        // now add the attributes back to the array - leaving out the one we're
        // removing
        for (var intLoop = 0; intLoop < intCount; intLoop++) {
            if (attributes[intLoop] != attributeName) {
                this.addAttribute(attributes[intLoop], tmpAttributeValues[intLoop]);
            }
        }
        return true;
    };
}

/**
 * beschreibt einen Dokument Typen
 * @param srch  die Parameter
 * @constructor
 */
function ArchivTyp(srch) {
    var i;
    if (REC.exist(srch.debugLevel))
        this.debugLevel = REC.getDebugLevel(srch.debugLevel);
    this.name = srch.name;
    this.searchString = srch.searchString;
    this.type = srch.type;
    if (REC.exist(srch.unique))
        this.unique = REC.trim(srch.unique);
    else
        this.unique = "error";
    if (REC.exist(srch.removeBlanks))
        this.removeBlanks = srch.removeBlanks;
    this.completeWord = REC.stringToBoolean(srch.completeWord, false);
    this.caseSensitive = REC.stringToBoolean(srch.caseSensitive, false);
    var tmp = [];
    REC.log(TRACE, "Search Archivposition");
    if (REC.exist(srch.archivPosition)) {
        REC.log(TRACE, "Archivposition exist");
        for (i = 0; i < srch.archivPosition.length; i++)
            tmp.push(new ArchivPosition(srch.archivPosition[i]));
        if (tmp.length > 0) {
            REC.log(DEBUG, tmp.length + " Archivposition found");
            this.archivPosition = tmp;
        } else
            REC.log(WARN, "No valid Archivposition found");
    }
    tmp = [];
    REC.log(TRACE, "Search Archivziel");
    if (REC.exist(srch.archivZiel)) {
        REC.log(TRACE, "Archivziel exist");
        for (i = 0; i < srch.archivZiel.length; i++)
            tmp.push(new ArchivZiel(srch.archivZiel[i]));
        if (tmp.length > 0) {
            REC.log(DEBUG, tmp.length + " Archivziel found");
            this.archivZiel = tmp;
        } else
            REC.log(WARN, "No valid Archivziel found");
    }
    REC.log(TRACE, "Search Tags");
    tmp = [];
    if (REC.exist(srch.tags)) {
        REC.log(TRACE, "Tags exist");
        for (i = 0; i < srch.tags.length; i++)
            tmp.push(new Tags(srch.tags[i]));
        if (tmp.length > 0) {
            REC.log(DEBUG, tmp.length + " Tags found");
            this.tags = tmp;
        } else
            REC.log(WARN, "No valid Tags found");
    }
    tmp = [];
    REC.log(TRACE, "Search Category");
    if (REC.exist(srch.category)) {
        REC.log(TRACE, "Category exist");
        for (i = 0; i < srch.category.length; i++)
            tmp.push(new Category(srch.category[i]));
        if (tmp.length > 0) {
            REC.log(DEBUG, tmp.length + " Category found");
            this.category = tmp;
        } else
            REC.log(WARN, "No valid Category found");
    }
    tmp = [];
    REC.log(TRACE, "Search SearchItems");
    if (REC.exist(srch.searchItem)) {
        REC.log(TRACE, "SearchItems exist");
        for (i = 0; i < srch.searchItem.length; i++)
            tmp.push(new SearchItem(srch.searchItem[i]));
        if (tmp.length > 0) {
            REC.log(DEBUG, tmp.length + " SearchItem found");
            this.searchItem = tmp;
        } else
            REC.log(WARN, "No valid SearchItem found");
    }
    tmp = [];
    REC.log(TRACE, "Search Archivtyp");
    if (REC.exist(srch.archivTyp)) {
        REC.log(TRACE, "Archivtyp exist");
        for (i = 0; i < srch.archivTyp.length; i++)
            tmp.push(new ArchivTyp(srch.archivTyp[i]));
        if (tmp.length > 0) {
            REC.log(DEBUG, tmp.length + " Archivtyp found");
            this.archivTyp = tmp;
        } else
            REC.log(WARN, "No valid Archivtyp found");
    }

    /**
     * Stringrepräsentation des Objektes
     * @param ident         Einrückung
     * @return {string}     das Objekt als String
     */
    this.toString = function (ident) {
        var i;
        if (!REC.exist(ident))
            ident = 0;
        ident++;
        var txt = REC.getIdent(ident) + "ArchivTyp:\n";
        txt = txt + REC.getIdent(ident) + "debugLevel: " + this.debugLevel + "\n";
        txt = txt + REC.getIdent(ident) + "name: " + this.name + "\n";
        txt = txt + REC.getIdent(ident) + "searchString: " + this.searchString + "\n";
        txt = txt + REC.getIdent(ident) + "type: " + this.name + "\n";
        txt = txt + REC.getIdent(ident) + "unique: " + this.unique + "\n";
        txt = txt + REC.getIdent(ident) + "removeBlanks: " + this.removeBlanks + "\n";
        txt = txt + REC.getIdent(ident) + "caseSensitive: " + this.caseSensitive + "\n";
        txt = txt + REC.getIdent(ident) + "completeWord: " + this.completeWord + "\n";
        if (REC.exist(this.archivPosition)) {
            for (i = 0; i < this.archivPosition.length; i++) {
                txt = txt + this.archivPosition[i].toString(ident);
            }
        }
        if (REC.exist(this.archivZiel)) {
            for (i = 0; i < this.archivZiel.length; i++) {
                txt = txt + this.archivZiel[i].toString(ident);
            }
        }
        if (REC.exist(this.tags)) {
            for (i = 0; i < this.tags.length; i++) {
                txt = txt + this.tags[i].toString(ident);
            }
        }
        if (REC.exist(this.category)) {
            for (i = 0; i < this.category.length; i++) {
                txt = txt + this.category[i].toString(ident);
            }
        }
        if (REC.exist(this.searchItem)) {
            for (i = 0; i < this.searchItem.length; i++) {
                txt = txt + this.searchItem[i].toString(ident);
            }
        }
        if (REC.exist(this.archivTyp)) {
            for (i = 0; i < this.archivTyp.length; i++) {
                txt = txt + this.archivTyp[i].toString(ident);
            }
        }
        return txt;
    };

    /**
     * erstellt eine neue Version eines Knotens
     * @param doc               der Knoten
     * @param newDoc            die neue Version des Knotens
     * @return {boolean}        [true] wenn alles geklappt hat, [false] im Fehlerfall
     */
    this.makeNewVersion = function (doc, newDoc) {
        if (doc.isLocked) {
            this.errors.push("Gelocktes Dokument kann nicht verändert werden!");
            return false;
        } else {
            if (!doc.hasAspect("cm:workingcopy")) {
                doc.ensureVersioningEnabled(true, false);
                var workingCopy = doc.checkoutForUpload();
                workingCopy.properties.content.write(REC.currentDocument.properties.content);
                workingCopy.checkin();
                newDoc.remove();
                REC.log(INFORMATIONAL, "Neue Version des Dokumentes erstellt");
                return true;
            }
        }
    };

    /**
     * handelt das Dokument
     * @param {array} documente     eine Liste der gefundenen bereits vorhandene gleichen Dokumente
     * @param destination          das ermittelte Ziel Verzeichnis für das hochgeladene Dokument
     */
    this.handleDocument = function(documente, destination) {
        var move;
        if (documente.length > 0) {
            move = false;
            for (var i = 0; i < documente.length; i++) {
                var document = documente[i];
                if (this.unique == "newVersion") {
                    // neue Version erstellen
                    REC.log(WARN, "Dokument ist bereits vorhanden! Erstelle neue Version...");
                    if (!this.makeNewVersion(document, REC.currentDocument))
                        break;
                } else if (this.unique == "overWrite") {
                    // überschreiben
                    REC.log(WARN, "Dokument ist bereits vorhanden! Dokument wird ersetzt...");
                    document.remove();
                    move = true
                } else if (this.unique == "nothing") {
                    // nichts machen und hochgeladenes Dokument löschen
                    REC.log(WARN, "Dokument mit gleichem Titel existiert bereits, hochgeladenes Dokument wird gel\\u00F6scht!");
                    REC.currentDocument.remove();
                    break;
                } else {
                    // this.unique == "error"
                    // Fehler werfen und hochgeladenes Dokument in die Duplicate Box stellen
                    REC.errors.push("Dokument mit dem " + (REC.currentDocument.name == document.name ? "Dateinamen " + REC.currentDocument.name : "Titel " + REC.currentDocument.properties["cm:title"]) + " ist im Zielordner bereits vorhanden! ");
                    REC.log(TRACE, "ArchivTyp.resolve: move document to folder " + REC.completeNodePath(REC.duplicateBox));
                    if (!REC.currentDocument.move(REC.duplicateBox))
                        REC.errors.push("Dokument konnte nicht in den Zielordner verschoben werden " + REC.completeNodePath(REC.duplicateBox));
                    break;
                }
            }
        } else {
            // keine möglichen doppelten Dokumente gefunden
            move = true;
        }
        if (move) {
            REC.log(TRACE, "ArchivTyp.resolve: move document to folder");
            if (!REC.currentDocument.move(destination))
                REC.errors.push("Dokument konnte nicht in den Zielordner verschoben werden " + REC.completeNodePath(destination));
        }
    };

    /**
     * führt die Erkennung des Dokumentes durch
     * @returns {boolean} [true]   der ArchivTyp passt und keine weitere Verarbeitung nötig
     *                    [false]  das Dokument konnte nicht erkannt werden und die Erkennung wird mit anderen
     *                             ArchivTypen versucht
     */
    this.resolve = function () {
        var i;
        var found = false;
        var erg;
        var orgLevel = REC.debugLevel;
        if (REC.exist(this.debugLevel))
            REC.debugLevel = this.debugLevel;
        REC.log(DEBUG, "resolve ArchivTyp " + this.name);
        REC.log(TRACE, "ArchivTyp.resolve: settings are: \n" + this);
        var str = REC.exist(this.removeBlanks) ? REC.content.replace(/ /g, '') : REC.content;
        var pst = (this.completeWord ? "\\b" + this.searchString + "?\\b" : this.searchString);
        var pat = new RegExp(pst, (this.caseSensitive ? "" : "i"));
        if (REC.exist(str) && pat.test(str)) {
            found = true;
            if (this.name != "Fehler")
                REC.currXMLName.push(this.name);
            REC.log(INFORMATIONAL, "Regel gefunden " + this.name);
            if (REC.exist(REC.currentSearchItems)) {
                REC.currentSearchItems = REC.currentSearchItems.concat(this.searchItem);
            } else
                REC.currentSearchItems = this.searchItem;
            if (REC.exist(this.archivZiel)) {
                for (i = 0; i < this.archivZiel.length; i++) {
                    REC.log(TRACE, "ArchivTyp.resolve: call ArchivZiel.resolve with " + REC.currentDocument.toString());
                    this.archivZiel[i].resolve(REC.currentDocument);
                }
            }
            if (REC.exist(this.archivTyp)) {
                for (i = 0; i < this.archivTyp.length; i++) {
                    REC.log(TRACE, "ArchivTyp.resolve: call ArchivTyp.resolve ");
                    if (this.archivTyp[i].resolve()) {
                        this.unique = this.archivTyp[i].unique;
                        break;
                    }
                }
            }
            if (REC.exist(this.searchItem)) {
                for (i = 0; i < this.searchItem.length; i++) {
                    REC.log(TRACE, "ArchivTyp.resolve: call SearchItem.resolve ");
                    this.searchItem[i].resolve();
                }
            }
            if (REC.exist(this.tags)) {
                for (i = 0; i < this.tags.length; i++) {
                    REC.log(TRACE, "ArchivTyp.resolve: call Tags.resolve with currentDocument");
                    this.tags[i].resolve(REC.currentDocument);
                }
            }
            if (REC.exist(this.category)) {
                for (i = 0; i < this.category.length; i++) {
                    REC.log(TRACE, "ArchivTyp.resolve: call Category.resolve with currentDocument");
                    this.category[i].resolve(REC.currentDocument);
                }
            }
            // Unique in die Parents übertragen
            var p = this.parent;
            while (REC.exist(p)) {
                p.unique = this.unique;
                p = p.parent;
            }
            if (REC.exist(this.archivPosition)) {
                var destinationFolder = null;
                for (i = 0; i < this.archivPosition.length; i++) {
                    REC.log(TRACE, "ArchivTyp.resolve: call ArchivPosition.resolve");
                    destinationFolder = this.archivPosition[i].resolve();
                    if (REC.exist(destinationFolder)) {
                        REC.log(TRACE, "ArchivTyp.resolve: process archivPosition" + REC.completeNodePath(destinationFolder));
                        if (REC.exist(REC.mandatoryElements) && this.name != REC.errorBox.name && this.name != REC.duplicateBox.name) {
                            for (var j = 0; j < REC.mandatoryElements.length; j++) {
                                if (!REC.exist(REC.currentDocument.properties[REC.mandatoryElements[j]])) {
                                    REC.errors.push(REC.mandatoryElements[j] + " is missing!");
                                }
                            }
                            if (REC.errors.length > 0) {
                                if (!REC.currentDocument.move(REC.errorBox))
                                    REC.errors.push("Dokument konnte nicht in den Zielordner verschoben werden " + REC.completeNodePath(REC.errorBox));
                                return found;
                            }
                        }
                        //TODO Das nochmal ändern
                        if (this.name != REC.errorBox.name && this.name != REC.duplicateBox.name) {
                            var COM = new Comments();
                            COM.removeComments(REC.currentDocument);
                        }
                        if (this.archivPosition[i].link && REC.exist(destinationFolder)) {
                            REC.log(INFORMATIONAL, "Document link to folder " + REC.completeNodePath(destinationFolder));
                            if (REC.exist(companyhome.childByNamePath(destinationFolder.displayPath + "/" + REC.currentDocument.name)))
                                REC.log(WARN, "Link already exists!");
                            else
                                destinationFolder.addNode(REC.currentDocument);
                        } else {
                            REC.log(INFORMATIONAL, "Document place to folder " + REC.completeNodePath(destinationFolder));
                            REC.log(TRACE, "ArchivTyp.resolve: search Document: " + REC.currentDocument.name + " in " + REC.completeNodePath(destinationFolder));
                            var tmpDoc = destinationFolder.childByNamePath(REC.currentDocument.name);
                            if (tmpDoc != null) {
                                this.handleDocument([tmpDoc], destinationFolder);
                            } else {
                                var searchTitleResult = [];
                                if (REC.exist(this.unique) && REC.exist(REC.results["title"])) {
                                    REC.log(TRACE, "ArchivTyp.resolve: check for unique");
                                    var searchCriteria = "+PATH:\"/" + destinationFolder.qnamePath + "//*\" +@cm\\:title:\"" + REC.results["title"].val + "\"";
                                    REC.log(TRACE, "ArchivTyp.resolve: search document with " + searchCriteria);
                                    searchTitleResult = search.luceneSearch(searchCriteria);
                                    if (searchTitleResult.length > 0) {
                                        REC.log(TRACE, "ArchivTyp.resolve: search document found " + searchTitleResult.length + " documents");
                                        for (var k = 0; k < searchTitleResult.length; k++) {
                                            // TODO prüfen, ob man das machen muss
                                            REC.log(TRACE, "ArchivTyp.resolve: compare with document " + searchTitleResult[k].name + "[" + searchTitleResult[k].properties['cm:title'] + "]...");
                                            if (searchTitleResult[k].properties["cm:title"] != REC.results["title"].val) {
                                                searchTitleResult.splice(k, 1)
                                            }
                                        }
                                    } else {
                                        REC.log(TRACE, "ArchivTyp.resolve: check for unique: no document with same title found");
                                    }
                                }
                                this.handleDocument(searchTitleResult, destinationFolder);
                            }
                        }
                    } else {
                        REC.errors.push("kein Zielordner vorhanden!");
                    }
                }
            }
        }
        REC.debugLevel = orgLevel;
        return found;
    };
}

/**
 * Ermittelt die Zielposition für das Dokument
 * @param srch      die Parameter
 * @constructor
 */
function ArchivPosition(srch) {
    if (REC.exist(srch.debugLevel))
        REC.debugLevel = REC.getDebugLevel(srch.debugLevel);
    this.link = REC.stringToBoolean(srch.link, false);
    if (REC.exist(srch.folder))
        this.folder = srch.folder;
    var tmp = [];
    REC.log(TRACE, "Search Archivziel");
    if (REC.exist(srch.archivZiel)) {
        REC.log(TRACE, "Archivziel exist");
        for (var i = 0; i < srch.archivZiel.length; i++)
            tmp.push(new ArchivZiel(srch.archivZiel[i]));
        if (tmp.length > 0) {
            REC.log(DEBUG, tmp.length + " Archivziel found");
            this.archivZiel = tmp;
        } else
            REC.log(WARN, "No valid Archivziel found");
    }

    /**
     * Stringrepräsentation des Objektes
     * @param ident         Einrückung
     * @return {string}     das Objekt als String
     */
    this.toString = function (ident) {
        if (!REC.exist(ident))
            ident = 0;
        ident++;
        var txt = REC.getIdent(ident) + "ArchivPosition:\n";
        txt = txt + REC.getIdent(ident) + "debugLevel: " + this.debugLevel + "\n";
        txt = txt + REC.getIdent(ident) + "link: " + this.link + "\n";
        txt = txt + REC.getIdent(ident) + "folder: " + this.folder + "\n";
        if (REC.exist(this.archivZiel)) {
            for (var i = 0; i < this.archivZiel.length; i++) {
                txt = txt + this.archivZiel[i].toString(ident);
            }
        }
        return txt;
    };

    /**
     * liefert den Alfresco Folder, bzw. erstellt die Folderstruktur, falls noch nicht vorhanden
     * @param folderName            der Folder als String
     * @return {*}                  der Alfresco Folder
     */
    this.resolveFolder = function (folderName) {
        REC.log(TRACE, "buildFolder: entering with " + folderName);
        var fol = null;
        var dir = folderName;
        var top = companyhome.childByNamePath(folderName);
        if (top == null) {
            REC.log(TRACE, "buildFolder: folder " + folderName + " not found");
            var parts = folderName.split("/");
            dir = "";
            for (var i = 0; i < parts.length; i++) {
                var part = parts[i];
                if (part.length > 0) {
                    dir = dir + (dir.length == 0 ? "" : "/") + part;
                    REC.log(TRACE, "buildFolder: search Folder " + dir);
                    if (dir.length > 0)
                        fol = companyhome.childByNamePath(dir);
                    if (!REC.exist(fol)) {
                        REC.log(INFORMATIONAL, "erstelle Folder " + dir);
                        if (top == null) {
                            REC.log(TRACE, "buildFolder: create Folder[" + part + "] at companyhome ");
                            top = companyhome.createFolder(part);
                        } else {
                            REC.log(TRACE, "buildFolder: create Folder[" + part + "] at " + top.name);
                            top = top.createFolder(part);
                        }
                        if (top == null) {
                            REC.errors.push("Folder " + dir + " konnte nicht erstellt werden");
                            break;
                        }
                    } else {
                        REC.log(TRACE, "buildFolder: folder " + dir + " found");
                        top = fol;
                    }
                }
            }
        }
        REC.log(TRACE, "buildFolder result is " + dir);
        return top;
    };

    /**
     * baut einen Foldernamen auf
     * @return {*}   der Alfresco Folder, bzw null wenn er nicht aufgebaut werden konnte
     */
    this.buildFolder = function() {
        var erg;
        var tmp = (REC.exist(REC.archivRoot) ? REC.completeNodePath(REC.archivRoot) : "");
        REC.log(TRACE, "ArchivPosition.resolve: result is " + tmp);
        if (REC.exist(this.folder)) {
            var tmp1 = REC.replaceVar(this.folder);
            if (!tmp1[1]) {
                erg = "Variabel konnte nicht im Foldernamen ersetzt werden!\n";
                REC.errors.push(erg);
                return;
            }
            tmp = tmp + "/" + tmp1[0];
            var exp = new RegExp("[*\"<>\?:|]|\\.$");
            if (tmp.match(exp)) {
                var m = exp.exec(tmp);
                erg = "Ung\ufffdtige Zeichen f\ufffdr Foldernamen!\n";
                erg = erg + tmp + "\n";
                erg = erg + "Position " + m.index + ":\n";
                for (var i = 0; i < m.length; i++) {
                    erg = erg + m[i] + "\n";
                }
                REC.errors.push(erg);
                return;
            }
        }
        REC.log(TRACE, "ArchivPosition.resolve: result is " + tmp);
        tmp = this.resolveFolder(tmp);
        REC.log(TRACE, "ArchivPosition.resolve: result is " + tmp);
        return tmp;
    };

    /**
     * ermittelt die Position des Dokumentes im Archiv
     * @return {node}  der Folder, in das das Dokument eingestellt werden soll
     */
    this.resolve = function () {
        var erg, folder;
        var orgLevel = REC.debugLevel;
        if (REC.exist(this.debugLevel))
            REC.debugLevel = this.debugLevel;
        REC.log(DEBUG, "resolve ArchivPosition");
        REC.log(TRACE, "ArchivPosition.resolve: settings are: \n" + this);
        folder = this.buildFolder();
        if (REC.exist(this.archivZiel) && REC.exist(folder)) {
            for (i = 0; i < this.archivZiel.length; i++) {
                REC.log(TRACE, "ArchivPosition.resolve: call ArchivZiel.resolve with " + REC.completeNodePath(folder));
                this.archivZiel[i].resolve(folder);
            }
        }
        REC.log(DEBUG, "ArchivPosition.resolve: return is " + (REC.exist(folder) ? REC.completeNodePath(folder) : "<null>"));
        REC.debugLevel = orgLevel;
        return folder;
    };
}

/**
 * formatiert Werte
 * @param srch      die Parameter
 * @constructor
 */
function Format(srch) {
    if (REC.exist(srch.debugLevel))
        this.debugLevel = REC.getDebugLevel(srch.debugLevel);
    this.formatString = srch.formatString;

   /**
    * Stringrepräsentation des Objektes
    * @param ident         Einrückung
    * @return {string}     das Objekt als String
    */
   this.toString = function (ident) {
        if (!REC.exist(ident))
            ident = 0;
        ident++;
        var txt = REC.getIdent(ident) + "Format:\n";
        txt = txt + REC.getIdent(ident) + "debugLevel: " + this.debugLevel + "\n";
        txt = txt + REC.getIdent(ident) + "formatString: " + this.formatString + "\n";
        return txt;
    };

    /**
     * formatiert den Wert
     * @param value     der Wert
     * @return {*}      der Wert als formatierter String
     */
    this.resolve = function (value) {
        var orgLevel = REC.debugLevel;
        if (REC.exist(this.debugLevel))
            REC.debugLevel = this.debugLevel;
        REC.log(DEBUG, "resolve Format with " + value);
        REC.log(TRACE, "Format.resolve: settings are: \n" + this);
        var erg = null;
        if (REC.isDate(value))
            erg = REC.dateFormat(value, this.formatString);
        if (typeof value == "number")
            erg = REC.numberFormat(value, this.formatString);
        REC.log(DEBUG, "Format.resolve: return " + erg);
        REC.debugLevel = orgLevel;
        return erg;
    };

}

/**
 * Setzt das ArchivZiel
 * @param srch      die Parameter
 * @constructor
 */
function ArchivZiel(srch) {
    if (REC.exist(srch.debugLevel))
        this.debugLevel = REC.getDebugLevel(srch.debugLevel);
    if (REC.exist(srch.aspect))
        this.aspect = srch.aspect;
    if (REC.exist(srch.type))
        this.type = srch.type;

    /**
     * Stringrepräsentation des Objektes
     * @param ident         Einrückung
     * @return {string}     das Objekt als String
     */
    this.toString = function (ident) {
        if (!REC.exist(ident))
            ident = 0;
        ident++;
        var txt = REC.getIdent(ident) + "ArchivZiel:\n";
        txt = txt + REC.getIdent(ident) + "debugLevel: " + this.debugLevel + "\n";
        txt = txt + REC.getIdent(ident) + "aspect: " + this.aspect + "\n";
        txt = txt + REC.getIdent(ident) + "type: " + this.type + "\n";
        return txt;
    };

    /**
     * setzt das Archiv Ziel
     * @param node   der Knoten, für das das Archivziel gesetzt werden soll
     * @return {boolean} false, wenn das Archivziel nicht gesetzt werden konnte.
     */
    this.resolve = function (node) {
        var orgLevel = REC.debugLevel;
        if (REC.exist(this.debugLevel))
            REC.debugLevel = this.debugLevel;
        REC.log(DEBUG, "resolve ArchivZiel");
        REC.log(TRACE, "ArchivZiel.resolve: settings are: \n" + this);
        if (!REC.exist(node)) {
            REC.errors.push("ArchivZiel.resolve: Node not found!");
            return false;
        }
        if (REC.exist(this.aspect)) {
            REC.log(TRACE, "ArchivZiel.resolve: Aspect is " + this.aspect);
            if (!node.hasAspect(this.aspect))
                node.addAspect(this.aspect);
            REC.log(INFORMATIONAL, "add aspect " + this.aspect);
        }

        if (REC.exist(this.type)) {
            REC.log(TRACE, "ArchivZiel.resolve: Type is " + this.type);
            if (!node.isSubType(this.type))
                node.specializeType(this.type);
            REC.log(INFORMATIONAL, "specialize type " + this.type);
        }

        REC.debugLevel = orgLevel;
        return true;
    };
}

/**
 * Stellt Funktionalität zur Gültigkeitsprüfung von gefundenen Ergebnissen zur Verfügung
 * @param srch      die Parameter
 * @param parent    das dazu gehörende SearchItem
 * @constructor
 */
function Check(srch, parent) {

    if (REC.exist(srch.debugLevel))
        this.debugLevel = REC.getDebugLevel(srch.debugLevel);
    if (REC.exist(parent))
        this.parent = parent;
    else
        this.parent.objectTyp = "string";
    if (this.parent.objectTyp == "date") {
        this.lowerValue = (REC.exist(srch.lowerValue) ? new Date(REC.trim(srch.lowerValue)) : null);
        this.upperValue = (REC.exist(srch.upperValue) ? new Date(REC.trim(srch.upperValue)) : null);
    } else if (this.parent.objectTyp == "int") {
        this.lowerValue = (REC.exist(srch.lowerValue) ? parseInt(REC.trim(srch.lowerValue), 10) : null);
        this.upperValue = (REC.exist(srch.upperValue) ? parseInt(REC.trim(srch.upperValue), 10) : null);
    } else if (this.parent.objectTyp == "float") {
        this.lowerValue = (REC.exist(srch.lowerValue) ? parseFloat(REC.trim(srch.lowerValue)) : null);
        this.upperValue = (REC.exist(srch.upperValue) ? parseFloat(REC.trim(srch.upperValue)) : null);
    } else {
        this.lowerValue = (REC.exist(srch.lowerValue) ? srch.lowerValue : null);
        this.upperValue = (REC.exist(srch.upperValue) ? srch.upperValue : null);
    }

    /**
     * führt die eigentliche Prüfung durch
     */
    this.resolve = function () {
        var orgLevel = REC.debugLevel;
        if (REC.exist(this.debugLevel))
            REC.debugLevel = this.debugLevel;
        REC.log(DEBUG, "resolve Check with " + this.parent.erg + " and " + this.parent.name);
        REC.log(TRACE, "Check.resolve: settings are:\n" + this);
        for (var i = 0; i < this.parent.erg.length; i++) {
            if (this.parent.erg[i].check) {
                if (REC.exist(this.upperValue) && this.parent.erg[i].getValue() > this.upperValue) {
                    this.parent.erg[i].check = false;
                    this.parent.erg[i].error = this.parent.name + " maybe wrong [" + this.parent.erg[i].getValue() + "] is bigger " + this.upperValue;
                }
                if (REC.exist(this.lowerValue) && this.parent.erg[i].getValue() < this.lowerValue) {
                    this.parent.erg[i].check = false;
                    this.parent.erg[i].error = this.parent.name + " maybe wrong [" + this.parent.erg[i].getValue() + "] is smaller " + this.lowerValue;
                }
            }
        }
        REC.log(DEBUG, "Check.resolve: return for " + this.parent.name + " is " + this.parent.erg.getResult().text);
        REC.debugLevel = orgLevel;
    };

    /**
     * Stringrepräsentation des Objektes
     * @param ident         Einrückung
     * @return {string}     das Objekt als String
     */
    this.toString = function (ident) {
        if (!REC.exist(ident))
            ident = 0;
        ident++;
        var txt = REC.getIdent(ident) + "Check:\n";
        txt = txt + REC.getIdent(ident) + "debugLevel: " + this.debugLevel + "\n";
        txt = txt + REC.getIdent(ident) + "lowerValue: " + this.lowerValue + "\n";
        txt = txt + REC.getIdent(ident) + "upperValue: " + this.upperValue + "\n";
        return txt;
    }
}

/**
 * stellt die Funktionalität für die Kommentare zur Verfügung
 * @constructor
 */
function Comments() {

    /**
     * fügt einem Knoten einen Kommentar hinzu
     * @param node          der Knoten
     * @param content       der Kommentar
     */
    this.addComment = function (node, content) {
        // fetch the data required to create a comment
        var title = "";
        // fetch the parent to add the node to
        var commentsFolder = this.getOrCreateCommentsFolder(node);
        // get a unique name
        var name = this.getUniqueChildName(commentsFolder, "comment");
        // create the comment
        var commentNode = commentsFolder.createNode(name, "fm:post", "cm:contains");
        commentNode.mimetype = "text/html";
        commentNode.properties.title = title;
        commentNode.content = content;
        commentNode.save();
    };

    /**
     * entfernt die Kommentare von einem Knoten
     * @param node      der Knoten
     */
    this.removeComments = function (node) {
        var nodes = this.getComments(node);
        if (REC.exist(nodes)) {
            for (var i = 0; i < nodes.length; i++) {
                if (nodes[i].content.indexOf("<table border=\"1\"> <tr><td>Nummer</td><td>Fehler</td></tr> ") != -1)
                    nodes[i].remove();
            }
        }
    };

    this.getUniqueChildName = function (parentNode, prefix, date) {
        // we create a name looking like prefix-datetimestamp
        if (typeof date === 'undefined') {
            date = new Date();
        }
        var name = prefix + "-" + date.getTime();
        // check that no child for the given name exists
        if (parentNode.childByNamePath(name) === null) {
            return name;
        }
        // if there is already a prefix-datetimestamp node then start looking for a
        // unique
        // name by appending random numbers - try a maximum of 100 times.
        var finalName = name + "_" + Math.floor(Math.random() * 1000);
        var count = 0;
        while (parentNode.childByNamePath(finalName) !== null && count < 100) {
            finalName = name + "_" + Math.floor(Math.random() * 1000);
            ++count;
        }
        return finalName;
    };

    /**
     * liefert die Kommentare zu einem Knoten
     * @param node          der Knoten
     * @returns {*}         die Kommentare
     */
    this.getComments = function (node) {
        var commentsFolder = this.getCommentsFolder(node);
        if (commentsFolder !== null) {
            var elems = commentsFolder.childAssocs["cm:contains"];
            if (elems !== null) {
                return elems;
            }
        }
        // no comments found, return an empty array
        return [];
    };

    this.getCommentsFolder = function (node) {
        if (node.hasAspect("fm:discussable")) {
            var forumFolder = node.childAssocs["fm:discussion"][0];
            var topicFolder = forumFolder.childByNamePath("Comments");
            return topicFolder;
        } else {
            return null;
        }
    };

    this.getOrCreateCommentsFolder = function (node) {
        var commentsFolder = this.getCommentsFolder(node);
        if (commentsFolder == null) {
            commentsFolder = commentService.createCommentsFolder(node);
        }
        return commentsFolder;
    };

    this.getCommentData = function (node) {
        var data = {};
        data.node = node;
        data.author = people.getPerson(node.properties["cm:creator"]);
        data.isUpdated = (node.properties["cm:modified"] - node.properties["cm:created"]) > 5000;
        return data;
    };
}

/**
 * Ermittelt die Grenzen im Suchtext für das zu findende Element
 * @param srch      die Parameter
 * @constructor
 */
function Delimitter(srch) {
    Encoder.EncodeType = "numerical";
    if (REC.exist(srch.debugLevel))
        this.debugLevel = REC.getDebugLevel(srch.debugLevel);
    this.typ = srch.typ;
    this.text = Encoder.htmlDecode(srch.text);
    this.count = Number(srch.count);
    if (REC.exist(srch.removeBlanks))
        this.removeBlanks = srch.removeBlanks;

    /**
     * Stringrepräsentation des Objektes
     * @param ident         Einrückung
     * @return {string}     das Objekt als String
     */
    this.toString = function (ident) {
        if (!REC.exist(ident))
            ident = 0;
        ident++;
        var txt = REC.getIdent(ident) + "Delimitter:\n";
        txt = txt + REC.getIdent(ident) + "debugLevel: " + this.debugLevel + "\n";
        txt = txt + REC.getIdent(ident) + "typ: " + this.typ + "\n";
        txt = txt + REC.getIdent(ident) + "text: " + this.text + "\n";
        txt = txt + REC.getIdent(ident) + "count: " + this.count + "\n";
        txt = txt + REC.getIdent(ident) + "removeBlanks: " + this.removeBlanks + "\n";
        return txt;
    };

    /**
     * führt die Ermittlung der Grenzen für das zu suchende Objekt durch
     * @param erg           das bis jetzt gefundene Ergebnis
     * @param direction     die Suchrichtung für Logausgaben
     * @return {*}
     */
    this.resolve = function (erg, direction) {
        var orgLevel = REC.debugLevel;
        if (REC.exist(this.debugLevel))
            REC.debugLevel = this.debugLevel;
        REC.log(TRACE, "resolve Delimitter with " + erg);
        REC.log(TRACE, "Delimitter.resolve: settings are:\n" + this);
        if (REC.exist(this.removeBlanks) && this.removeBlanks == "before") {
            erg.removeBlanks();
        }
        for (var i = 0; i < erg.length; i++) {
            if (typeof erg[i].text == "string") {
                REC.log(DEBUG, "resolve Delimitter: current String is " + REC.printTrace(erg[i].text, direction));
                var txtSave = erg[i].text;
                var tmpPos;
                if (this.typ == "start") {
                    if (this.count < 0) {
                        erg[i].text = erg[i].text.split(this.text).reverse().slice(0, Math.abs(this.count)).reverse().join(this.text);
                        tmpPos = txtSave.split(this.text).reverse().slice(Math.abs(this.count)).reverse().join(this.text).length + this.text.length;
                    } else {
                        erg[i].text = erg[i].text.split(this.text).slice(Math.abs(this.count)).join(this.text);
                        tmpPos = txtSave.split(this.text).slice(0, Math.abs(this.count)).join(this.text).length + this.text.length;
                    }
                    erg[i].setStart(erg[i].getStart() + tmpPos);
                }
                if (this.typ == "end") {
                    if (this.count < 0) {
                        erg[i].text = erg[i].text.split(this.text).reverse().slice(Math.abs(this.count)).reverse().join(this.text);
                        tmpPos = txtSave.split(this.text).reverse().slice(0, Math.abs(this.count)).reverse().join(this.text).length;
                    } else {
                        erg[i].text = erg[i].text.split(this.text).slice(0, Math.abs(this.count)).join(this.text);
                        tmpPos = txtSave.split(this.text).slice(Math.abs(this.count)).join(this.text).length + this.text.length;
                    }
                    erg[i].setEnd(erg[i].getEnd() - tmpPos);
                }
                REC.log(DEBUG, "Delimitter.resolve: result is " + REC.printTrace(erg[i].text, direction));
            }
        }
        if (REC.exist(this.removeBlanks) && this.removeBlanks == "after") {
            erg.removeBlanks();
        }
        REC.log(TRACE, "Delimitter.resolve: return is  " + erg);
        REC.debugLevel = orgLevel;
        return erg;
    }
}

/**
 * stellt Funktionalität zum Verwalten der Kategorien zur Verfügung
 * @param srch      die Parameter
 * @constructor
 */
function Category(srch) {
    if (REC.exist(srch.debugLevel))
        this.debugLevel = REC.getDebugLevel(srch.debugLevel);
    this.name = srch.name;

    /**
     * Stringrepräsentation des Objektes
     * @param ident         Einrückung
     * @return {string}     das Objekt als String
     */
    this.toString = function (ident) {
        if (!REC.exist(ident))
            ident = 0;
        ident++;
        var txt = REC.getIdent(ident) + "Category:\n";
        txt = txt + REC.getIdent(ident) + "debugLevel: " + this.debugLevel + "\n";
        txt = txt + REC.getIdent(ident) + "name: " + this.name + "\n";
        return txt;
    };

    /**
     * kategorisiert das Dokument
     * @param document           das Dokument
     */
    this.resolve = function (document) {
        var orgLevel = REC.debugLevel;
        if (REC.exist(this.debugLevel))
            REC.debugLevel = this.debugLevel;
        REC.log(DEBUG, "resolve Category");
        REC.log(TRACE, "Category.resolve: settings are: \n" + this);
        if (REC.exist(this.name)) {
            REC.log(TRACE, "Category.resolve: Category is " + this.name);
            REC.log(INFORMATIONAL, "add Category " + this.name);
            var root = classification;
            if (root != null) {
                var top = root;
                var parents = this.name.split("/");
                for (var k = 0; k < parents.length; k++) {
                    var current = parents[k];
                    var nodes;
                    if (top == root)
                        nodes = top.getRootCategories("cm:generalclassifiable");
                    else
                        nodes = top.subCategories;
                    var nodeExists = false;
                    for (var nodeKey in nodes) {
                        if (nodes.hasOwnProperty(nodeKey)) {
                        var node = nodes[nodeKey];
                            if (node.name == current) {
                                REC.log(TRACE, "Category [" + current + "] found");
                                top = node;
                                nodeExists = true;
                                break;
                            }
                        }
                    }
                    if (!nodeExists) {
                        REC.log(INFORMATIONAL, "Category [" + current + "] not found! Create Category");
                        if (top == root) {
                            REC.log(TRACE, "Create Root Category...");
                            top = classification.createRootCategory("cm:generalclassifiable", current);
                            REC.log(TRACE, "Root Category created!");
                        } else {
                            REC.log(TRACE, top.name + ": Create Sub Category...");
                            top = top.createSubCategory(current);
                            REC.log(TRACE, "Sub Category created!");
                        }
                    }
                }
                if (top != null) {
                    REC.log(TRACE, "Add Aspect cm:generalclassifiable to document");
                    document.addAspect("cm:generalclassifiable");
                    var categories = [];
                    categories.push(top);
                    REC.log(INFORMATIONAL, "Add Category [" + this.name + "] to document");
                    document.properties["cm:categories"] = categories;
                    document.save();
                    REC.log(INFORMATIONAL, "Document saved!");
                } else
                    REC.errors.push("Category top not found!");
            } else
                REC.errors.push("Category root not found!");
        }
        REC.debugLevel = orgLevel;
    };
}

/**
 * stellt Funktionalität zum Bearbeiten der Tags eines Dokumentes zur Verfügung
 * @param srch      die Parameter
 * @constructor
 */
function Tags(srch) {
    if (REC.exist(srch.debugLevel))
        this.debugLevel = REC.getDebugLevel(srch.debugLevel);
    this.name = srch.name;

    /**
     * Stringrepräsentation des Objektes
     * @param ident         Einrückung
     * @return {string}     das Objekt als String
     */
    this.toString = function (ident) {
        if (!REC.exist(ident))
            ident = 0;
        ident++;
        var txt = REC.getIdent(ident) + "Tags:\n";
        txt = txt + REC.getIdent(ident) + "debugLevel: " + this.debugLevel + "\n";
        txt = txt + REC.getIdent(ident) + "name: " + this.name + "\n";
        return txt;
    };

    /**
     * taggt das Dokument
     * @param doc           das Dokument
     */
    this.resolve = function (doc) {
        var orgLevel = REC.debugLevel;
        if (REC.exist(this.debugLevel))
            REC.debugLevel = this.debugLevel;
        REC.log(DEBUG, "resolve Tags");
        REC.log(TRACE, "Tags.resolve: settings are: \n" + this);
        if (REC.exist(this.name)) {
            REC.log(TRACE, "Tags.resolve: Tag is " + this.name);
            doc.addTag(this.name);
            doc.save();
            REC.log(INFORMATIONAL, "Document saved!");
            REC.log(INFORMATIONAL, "add Tag " + this.name);
        }
        REC.debugLevel = orgLevel;
    };
}

/**
 * stellt die Funktionalität zum Suchen eines Dokumentes zur Verfügung
 * @param srch      die Parameter
 * @constructor
 */
function SearchItem(srch) {
    var tmp;
    var i;
    this.erg = new SearchResultContainer();
    this.resolved = false;
    if (REC.exist(srch.debugLevel))
        this.debugLevel = REC.getDebugLevel(srch.debugLevel);
    this.name = srch.name;
    this.readOverReturn = REC.stringToBoolean(srch.readOverReturn, false);
    this.required = REC.stringToBoolean(srch.required, true);
    if (REC.exist(srch.fix))
        this.fix = srch.fix;
    if (REC.exist(srch.kind)) {
        tmp = REC.trim(srch.kind).split(",");
        this.kind = [];
        this.kind.push(tmp[0]);
        if (tmp.length > 1)
            this.kind.push(parseInt(REC.trim(tmp[1]), 10));
        else
            this.kind.push(1);
    }
    if (REC.exist(srch.word)) {
        tmp = REC.trim(srch.word).split(",");
        this.word = [];
        for (i = 0; i < tmp.length; i++) {
            this.word.push(parseInt(REC.trim(tmp[i]), 10));
        }
        this.readOverReturn = true;
    }
    if (REC.exist(srch.eval))
        this.eval = srch.eval;
    if (REC.exist(srch.text))
        this.text = srch.text;
    if (REC.exist(srch.value))
        this.value = srch.value;
    if (REC.exist(srch.target))
        this.target = srch.target;
    if (REC.exist(srch.expected))
        this.expected = srch.expected;
    else
        this.expected = null;
    if (REC.exist(srch.objectTyp))
        this.objectTyp = srch.objectTyp;
    else {
        if (REC.exist(this.kind)) {
            if (this.kind[0] == "date")
                this.objectTyp = "date";
            if (this.kind[0] == "amount")
                this.objectTyp = "float";
        } else
            this.objectTyp = "string";
    }
    this.completeWord = REC.stringToBoolean(srch.completeWord, false);
    this.caseSensitive = REC.stringToBoolean(srch.caseSensitive, false);
    this.included = REC.stringToBoolean(srch.included, false);
    if (REC.exist(srch.removeBlanks))
        this.removeBlanks = srch.removeBlanks;
    if (REC.exist(srch.removeReturns))
        this.removeReturns = srch.removeReturns;
    this.backwards = REC.stringToBoolean(srch.backwards, false);
    this.left = (REC.exist(srch.direction) && REC.trim(srch.direction).toLowerCase() == "left");
    tmp = [];
    REC.log(TRACE, "Search Check");
    if (REC.exist(srch.check)) {
        REC.log(TRACE, "Check exist");
        for (i = 0; i < srch.check.length; i++)
            tmp.push(new Check(srch.check[i], this));
        if (tmp.length > 0) {
            REC.log(DEBUG, tmp.length + " Check found");
            this.check = tmp;
        } else
            REC.log(WARN, "No valid Check found");
    }
    tmp = [];
    REC.log(TRACE, "Search Delimitter");
    if (REC.exist(srch.delimitter)) {
        REC.log(TRACE, "Delimitter exist");
        for (i = 0; i < srch.delimitter.length; i++)
            tmp.push(new Delimitter(srch.delimitter[i]));
        if (tmp.length > 0) {
            REC.log(DEBUG, tmp.length + " Delimitter found");
            this.delimitter = tmp;
        } else
            REC.log(WARN, "No valid Delimitter found");
    }
    tmp = [];
    REC.log(TRACE, "Search Archivziel");
    if (REC.exist(srch.archivZiel)) {
        REC.log(TRACE, "Archivziel exist");
        for (i = 0; i < srch.archivZiel.length; i++)
            tmp.push(new ArchivZiel(srch.archivZiel[i]));
        if (tmp.length > 0) {
            REC.log(DEBUG, tmp.length + " Archivziel found");
            this.archivZiel = tmp;
        } else
            REC.log(WARN, "No valid Archivziel found");
    }
    tmp = [];
    REC.log(TRACE, "Format Archivziel");
    if (REC.exist(srch.format)) {
        REC.log(TRACE, "Format exist");
        for (i = 0; i < srch.format.length; i++)
            tmp.push(new Format(srch.format[i]));
        if (tmp.length > 0) {
            REC.log(DEBUG, tmp.length + " Format found");
            this.format = tmp;
        } else
            REC.log(WARN, "No valid Format found");
    }

    /**
     * Stringrepräsentation des Objektes
     * @param ident         Einrückung
     * @return {string}     das Objekt als String
     */
    this.toString = function (ident) {
        var i;
        if (!REC.exist(ident))
            ident = 0;
        ident++;
        var txt = REC.getIdent(ident) + "SearchItem:\n";
        txt = txt + REC.getIdent(ident) + "debugLevel: " + this.debugLevel + "\n";
        txt = txt + REC.getIdent(ident) + "name: " + this.name + "\n";
        txt = txt + REC.getIdent(ident) + "readOverReturn: " + this.readOverReturn + "\n";
        txt = txt + REC.getIdent(ident) + "fix: " + this.fix + "\n";
        txt = txt + REC.getIdent(ident) + "kind: " + this.kind + "\n";
        txt = txt + REC.getIdent(ident) + "word: " + this.word + "\n";
        txt = txt + REC.getIdent(ident) + "eval: " + this.eval + "\n";
        txt = txt + REC.getIdent(ident) + "text: " + this.text + "\n";
        txt = txt + REC.getIdent(ident) + "value: " + this.value + "\n";
        txt = txt + REC.getIdent(ident) + "expected: " + this.expected + "\n";
        txt = txt + REC.getIdent(ident) + "target: " + this.target + "\n";
        txt = txt + REC.getIdent(ident) + "objectTyp: " + this.objectTyp + "\n";
        txt = txt + REC.getIdent(ident) + "required: " + this.required + "\n";
        txt = txt + REC.getIdent(ident) + "removeBlanks: " + this.removeBlanks + "\n";
        txt = txt + REC.getIdent(ident) + "removeReturns: " + this.removeReturns + "\n";
        txt = txt + REC.getIdent(ident) + "backwards: " + this.backwards + "\n";
        txt = txt + REC.getIdent(ident) + "left: " + this.left + "\n";
        txt = txt + REC.getIdent(ident) + "caseSensitive: " + this.caseSensitive + "\n";
        txt = txt + REC.getIdent(ident) + "completeWord: " + this.completeWord + "\n";
        txt = txt + REC.getIdent(ident) + "included: " + this.included + "\n";
        if (REC.exist(this.check)) {
            for (i = 0; i < this.check.length; i++) {
                txt = txt + this.check[i].toString(ident);
            }
        }
        if (REC.exist(this.delimitter)) {
            for (i = 0; i < this.delimitter.length; i++) {
                txt = txt + this.delimitter[i].toString(ident);
            }
        }
        if (REC.exist(this.archivZiel)) {
            for (i = 0; i < this.archivZiel.length; i++) {
                txt = txt + this.archivZiel[i].toString(ident);
            }
        }
        if (REC.exist(this.format)) {
            for (i = 0; i < this.format.length; i++) {
                txt = txt + this.format[i].toString(ident);
            }
        }
        return txt;
    };


    /**
     * sucht nach einem speziellen Ergebnistyp
     * @param text              der zu duchsuchende Text
     * @param kind              die Art des Ergebnistypen Mögliche Werte [amount] Geldbetrag, [date] Datum, [float] Nummer
     * @param left              die Suchrichtung
     * @param expected          ein erwartetes Ergebnis für Testzwecke
     * @return {Array.<T>}
     */
    this.findSpecialType = function (text, kind, left, expected) {
        var ret = [];
        var erg = null;
        var exp = [];
        if (kind[0] == "date") {
            exp[0] = new RegExp("\\d{1,2}\\.?.[ ]{0,9}[A\\u00C4BCDEFGIJKLMNOPRSTUVYZa\\u00E4bcdefgijklmnoprstuvyz]+\\.?[ ]{0,9}(\\d{4}|\\d{2})|\\d{1,2}\\.\\d{1,2}\\.(\\d{4}|\\d{2})", "g");
            exp[1] = new RegExp("[A\\u00C4BCDEFGIJKLMNOPRSTUVYZa\\u00E4bcdefgijklmnoprstuvyz]+\\.?[ ]{0,9}(\\d{4}|\\d{2})|\\d{1,2}\\.\\d{1,2}\\.(\\d{4}|\\d{2})", "g");
        } else if (kind[0] == "amount") {
            exp[0] = new RegExp("((([0-9]{1,3}\\.)*[0-9]{1,3})|\\d+)(?:\\.|,(\\d{2}))?( Euro| EUR| \\u20AC)", "g");
        } else if (kind[0] == "float") {
            exp[0] = new RegExp("[\-0-9\.]+[\,]+[0-9]*");
        }
         for (var i = 0; i < exp.length; i++) {
            var match = text.match(exp[i]);
            var typ = null;
            if (REC.exist(match)) {
                for (var k = 0; k < match.length; k++) {
                    var result = exp[i].exec(text);
                    if (kind[0] == "date")
                        typ = "date";
                    else if (kind[0] == "amount" || kind[0] == "float")
                        typ = "float";
                    var res = new SearchResult(text, match[k], null, result.index, result.index + match[k].length, typ, expected);
                    res.convertValue();
                    if (REC.exist(res.val)) {
                        ret.push(res);
                    }
                }
                break;
            }
        }
        if (left)
            ret.reverse();
        return ret.slice(kind[1] - 1);
    };

    this.handleError = function () {
        REC.log(INFORMATIONAL, "SearchItem.resolve: " + this.name + " has NO RESULT");
        this.resolved = true;
        REC.results[this.name] = null;
        return null;
    };

    /**
     * führt die eigentliche Textsuche im Dokumenttext durch
     * @param txt       der zu suchende Text
     */
    this.find = function (txt) {
        var pos = 0;
        var lastPos = 0;
        var count = 0;
        var pst = (this.completeWord ? "\\b" + this.text + "?\\b" : this.text);
        var pat = new RegExp(pst, (this.caseSensitive ? "g" : "gi"));
        var match = txt.match(pat);
        var foundPos = [];
        if (REC.exist(match)) {
            for (var k = 0; k < match.length; k++) {
                var result = pat.exec(txt);
                foundPos.push(result.index);
            }
        }
        if (this.backwards) {
            REC.log(TRACE, "SearchItem.resolve: start search backwards with " + this.text);
            foundPos.reverse();
            match.reverse();
        } else {
            REC.log(TRACE, "SearchItem.resolve: start search forwards with " + this.text);
        }
        for (var j = 0; j < foundPos.length; j++) {
            pos = foundPos[j];
            REC.log(TRACE, "SearchItem.resolve: search found at position " + pos);
            var str;
            if (this.left) {
                str = new SearchResult(txt, txt.slice(lastPos, pos + (this.included ? match[j].length : 0)), null, lastPos, pos + (this.included ? match[j].length : 0), this.objectTyp,
                    this.expected);
                REC.log(TRACE, "SearchItem.resolve: get result left from position  " + REC.printTrace(str.text, this.left));
            } else {
                str = new SearchResult(txt, txt.substr(pos + (this.included ? 0 : match[j].length)), null, pos + (this.included ? 0 : match[j].length), txt.length, this.objectTyp, this.expected);
                REC.log(TRACE, "SearchItem.resolve: get result right from position  " + REC.printTrace(str.text, this.left));
            }
            if (REC.exist(str) && str.text.length > 0) {
                REC.log(TRACE, "SearchItem.resolve: possible result is " + REC.printTrace(str.text, this.left));
                this.erg.modifyResult(str, count++);
            }
            lastPos = this.left ? 0 : pos;
        }
    };

    /**
     * sucht ab einer bestimmten Position nach dem X. ganzen Wort
     * @param word   Array mit der Anzahl Wörtern [Anzahl Wörter bis zum Ergebnis, Anzahl Wörter die ins Ergebnis fliessen]
     * @param left   die Suchrichtung
     */
    this.findForWords = function ( word, left) {
        for (var i = 0; i < this.erg.length; i++) {
            if (typeof this.erg[i].text == "string") {
                this.erg[i].text = this.erg[i].text.replace(/\s/g, ' ');
                if (left)
                    this.erg[i].text = this.erg[i].text.split("").reverse().join("");
                var start = word[0];
                var end = 1;
                if (word.length > 1)
                    end = word[1];
                var tmp = this.erg[i].text.split('');
                var begin = 0;
                var ende = 0;
                var marker = false;
                for (var k = 0; k < tmp.length; k++) {
                    if (tmp[k] == " ") {
                        marker = true;
                    }
                    if (marker && tmp[k] != " ") {
                        begin++;
                        marker = false;
                    }
                    if (begin == start) {
                        start = k;
                        break;
                    }
                }
                marker = false;
                for (k = start; k <= tmp.length; k++) {
                    if (k == tmp.length)
                        end = k;
                    else {
                        if (tmp[k] != " ") {
                            marker = true;
                        }
                        if (marker && tmp[k] == " ") {
                            ende++;
                            marker = false;
                        }
                        if (ende == end) {
                            end = k;
                            break;
                        }
                    }
                }
                if (left) {
                    this.erg[i].text = tmp.slice(start, end).reverse().join("");
                    this.erg[i].setEnd(this.erg[i].getEnd() - tmp.slice(0, start).reverse().join("").length);
                    this.erg[i].setStart(this.erg[i].getStart() + tmp.slice(end).reverse().join("").length);
                } else {
                    this.erg[i].text = tmp.slice(start, end).join("");
                    this.erg[i].setStart(this.erg[i].getStart() + tmp.slice(0, start).join("").length);
                    this.erg[i].setEnd(this.erg[i].getEnd() - tmp.slice(end).join("").length);
                }
            }
        }
    };


    this.resolveItem = function (name) {
        for (var i = 0; i < REC.currentSearchItems.length; i++) {
            if (REC.currentSearchItems[i].name == name) {
                REC.currentSearchItems[i].resolve();
                return REC.results[name];
            }
        }
        REC.errors.push("SearchItem " + name + " not found!");
        return null;
    };

    this.resolve = function () {
        var i, e;
        var orgLevel = REC.debugLevel;
        if (REC.exist(this.debugLevel))
            REC.debugLevel = this.debugLevel;
        REC.log(DEBUG, "resolve SearchItem");
        REC.log(TRACE, "SearchItem.resolve: settings are: \n" + this);
        if (this.resolved) {
            if (REC.results[this.name] != null)
                return REC.results[this.name].getValue();
            else
                return null;
        }
        if (REC.exist(this.text))
            this.text = REC.replaceVar(this.text)[0];
        var txt = null;
        if (REC.exist(this.fix)) {
            var searchResult = new SearchResult(REC.replaceVar(this.fix)[0], REC.replaceVar(this.fix)[0], null, 0, 0, this.objectTyp, this.expected);
            searchResult.convertValue();
            this.erg.modifyResult(searchResult, 0);
        } else if (REC.exist(this.eval)) {
            e = eval(REC.replaceVar(this.eval)[0]);
            this.erg.modifyResult(new SearchResult(e.toString(), e.toString(), e, 0, 0, null, this.expected), 0);
        } else {
            if (REC.exist(this.value)) {
                e = this.resolveItem(this.value);
                if (REC.exist(e)) {
                    e = new SearchResult(e.document, e.text, e.val, e.getStart(), e.getEnd(), e.typ, e.expected);
                    if (REC.exist(this.expected))
                        e.expected = this.expected;
                    if (REC.exist(this.objectTyp))
                        e.typ = this.objectTyp;
                    this.erg.addResult(e);
                    txt = this.erg.getResult().text;
                } else
                    return this.handleError();
            } else
                txt = REC.content;
            if (REC.exist(this.removeBlanks) && this.removeBlanks == "before") {
                txt = txt.replace(/ /g, '');
            }
            if (REC.exist(this.removeReturns) && this.removeReturns == "before") {
                txt = txt.replace(/\n/g, '').replace(/\n/g, '');
            }
            if (REC.exist(this.kind))
                this.erg.modifyResult(this.findSpecialType(txt, this.kind, this.left, this.expected), 0);
            else if (REC.exist(this.text))
                this.find(txt);
        }
        if (this.erg.length == 0) {
            REC.log(TRACE, "searchItem.resolve: no matching result found");
        } else {
            if (REC.exist(this.delimitter)) {
                for (i = 0; i < this.delimitter.length; i++) {
                    REC.log(DEBUG, "SearchItem.resolve: call Delimitter.resolve with " + REC.printTrace(this.erg, this.left));
                    this.erg = this.delimitter[i].resolve(this.erg, this.left);
                }
            }
            if (!this.readOverReturn && REC.exist(this.text)) {
                for (i = 0; i < this.erg.length; i++) {
                    this.erg[i].limitToReturn();
                }
                REC.log(DEBUG, "SearchItem.resolve: readOverReturn result is " + REC.printTrace(this.erg, this.left));
            }
            if (REC.exist(this.word)) {
                this.findForWords(this.word, this.left);
            }
            if (REC.exist(this.removeBlanks) && this.removeBlanks == "after") {
                this.erg.removeBlanks();
            }
            if (REC.exist(this.removeReturns) && this.removeReturns == "after") {
                this.erg.removeReturns();
            }
            this.erg.convert();
            if (REC.exist(this.format)) {
                for (i = 0; i < this.format.length; i++) {
                    REC.log(DEBUG, "SearchItem.resolve: call Format.resolve with " + this.erg.getResult().getValue());
                    this.erg.getResult().val = this.format[i].resolve(this.erg.getResult().getValue());
                }
            }
            if (REC.exist(this.check)) {
                for (i = 0; i < this.check.length; i++) {
                    REC.log(DEBUG, "SearchItem.resolve: call Check.resolve");
                    this.check[i].resolve();
                }
            }
            REC.positions.add(REC.convertPosition(REC.content, this.erg.getResult().getStart(), this.erg.getResult().getEnd(), this.name, this.erg.getResult().check));

            if (REC.exist(this.archivZiel)) {
                for (i = 0; i < this.archivZiel.length; i++) {
                    REC.log(DEBUG, "SearchItem.resolve: call ArchivZiel.resolve with " + REC.completeNodePath(REC.currentDocument));
                    this.archivZiel[i].resolve(REC.currentDocument);
                }
            }
            if (REC.exist(this.target) && this.erg.isFound()) {
                REC.log(INFORMATIONAL, "currentDocument.properties[\"" + this.target + "\"] = \"" + this.erg.getResult().getValue() + "\";");
                REC.currentDocument.properties[this.target] = this.erg.getResult().getValue();
                REC.currentDocument.save();
                REC.log(INFORMATIONAL, "Document saved!");
            }
        }
        if (this.required && !this.erg.isFound()) {
            e = "Required SearchItem " + this.name + " is missing";
            REC.errors.push(e);
            REC.errors.push(this.erg.getError());
        } else if (this.erg.isFound()) {
            REC.log(DEBUG, "SearchItem.resolve: return is  " + this.erg.getResult().getValue());
            REC.debugLevel = orgLevel;
            REC.results[this.name] = this.erg.getResult();
            REC.log(INFORMATIONAL, this.name + " is " + this.erg.getResult().getValue());
            this.resolved = true;
            return this.erg.getResult().getValue();
        } else
            return this.handleError();
    };
}

/**
 * beschreibt die Position eines gefundenem Wertes in dem Dokument
 * @param startRow          die Zeile, in der der Wert beginnt
 * @param startColumn       die Spalte, in der der Wert beginnt
 * @param endRow            die Zeile, in der der Wert endet
 * @param endColumn         die Spalte, in der der Wert endete
 * @param type              der Typ des Wertes
 * @param desc              eine Beschreibung
 * @constructor
 */
function Position(startRow, startColumn, endRow, endColumn, type, desc) {
    this.startRow = startRow;
    this.startColumn = startColumn;
    this.endRow = endRow;
    this.endColumn = endColumn;
    this.type = type;
    this.desc = desc;

    this.print = function () {
        return "StartRow: " + this.startRow + " StartColumn: " + this.startColumn + " EndRow: " + this.endRow + " EndColumn: " + this.endColumn + " Description: " + this.desc;
    };
}



function PositionContainer() {}

PositionContainer.prototype = [];

PositionContainer.prototype.add = function (pos) {
    var found = false;
    if (!(pos.startRow == pos.endRow && pos.startColumn == pos.endColumn)) {
        for (var i = 0; i < this.length; i++) {
            if ((pos.startRow > this[i].startRow && pos.endRow < this[i].endRow) || (pos.startRow == this[i].startRow && pos.startColumn >= this[i].startColumn)
                && (pos.endRow == this[i].endRow && pos.endColumn <= this[i].endColumn)) {
                this[i] = pos;
                found = true;
                break;
            }
        }
        if (!found)
            this.push(pos);
    }
};

function SearchResultContainer() {}

SearchResultContainer.prototype = [];

SearchResultContainer.prototype.addResult = function (result) {
    if (result instanceof Array) {
        for (var i = 0; i < result.length; i++)
            this.push(result[i]);
    } else
        this.push(result);
};

SearchResultContainer.prototype.getResult = function () {
    for (var i = 0; i < this.length; i++) {
        if (this[i].check)
            return this[i];
    }
    if (REC.exist(this[0]))
        return this[0];
    return null;
};

SearchResultContainer.prototype.removeResult = function (result) {
    var h = [];
    for (var i = 0; i < this.length; i++) {
        if (this[i] != result)
            h.push(this[i]);
    }
    this.prototype = h;
};

SearchResultContainer.prototype.modifyResult = function (result, pos) {
    if (!REC.exist(this[pos]))
        this.addResult(result);
    else {
        if (result instanceof Array) {
            this.modifyResult(result[0], pos);
            for (var i = 1; i < result.length; i++) {
                this.addResult(result[i]);
            }
        } else {
            this[pos].text = result.text;
            this[pos].val = result.val;
            this[pos].check = result.check;
            this[pos].error = result.error;
            this[pos].typ = result.typ;
            this[pos].expected = result.expected;
            this[pos].setStart(this[pos].getStart() + result.getStart());
            this[pos].setEnd(this[pos].getStart() + result.getEnd() - result.getStart());
        }
    }
};

SearchResultContainer.prototype.isFound = function () {
    return (REC.exist(this.getResult()) && this.getResult().check);
};

SearchResultContainer.prototype.toString = function (ident) {
    var txt = "";
    if (!REC.exist(ident))
        ident = 0;
    ident++;
    for (var i = 0; i < this.length; i++) {
        txt = txt + REC.getIdent(ident) + this[i].toString() + "\n";
    }
    return txt;
};

SearchResultContainer.prototype.getError = function () {
    var e = this.getResult();
    if (e != null)
        return e.error;
    else
        return null;
};

/**
 * entfernt die Blanks aus den Ergebnissen
 */
SearchResultContainer.prototype.removeBlanks = function () {
    for (var i = 0; i < this.length; i++) {
        if (typeof this[i].text == "string") {
            REC.log(TRACE, "Removing Blanks from String...");
            this[i].mergeStr(' ');
        }
    }
};

/**
 * entfernt die Returns aus einem String
 */
SearchResultContainer.prototype.removeReturns = function () {
    for (var i = 0; i < this.length; i++) {
        if (typeof this[i].text == "string") {
            REC.log(TRACE, "Removing Returns from String...");
            this[i].mergeStr('\n');
            this[i].mergeStr('\n');
        }
    }
};

/**
 * konvertiert ein gefundenes Ergebnis in den vorgesehen Objecttypen
 */
SearchResultContainer.prototype.convert = function () {
    for (var i = 0; i < this.length; i++) {
        REC.log(TRACE, "SearchItem.resolve: call convertValue " + this[i].text + " and " + this.name);
        if (typeof this[i].text == "string" && REC.exist(this[i].text)) {
            // Leerzeichen entfernen
            this[i].makeTrim();
        }
        if (typeof this[i].text == "string") {
            // Konvertieren in den passenden Objekttypen
            this[i].convertValue();
        }
        // prüfen, ob die Konvertierung funktioniert hat
        if (this[i].typ == "date" && !REC.isDate(this[i].val)) {
            this[i].check = false;
            this[i].val = null;
            this[i].error = "Result for " + this.name + " [" + this[i].text + "] is not date";
        }
        if ((this[i].typ == "int" || this[i].typ == "float")) {
            if (!REC.isNumeric(this[i].val)) {
                this[i].check = false;
                this[i].val = null;
                this[i].error = "Result for " + this.name + " [" + this[i].text + "] is not a numeric value";
                // TODO Wird das noch gebraucht?
/*                if (REC.exist(this.text)) {
                    var numberExp = new RegExp("([\\-][1-9]{1}[0-9]{1,}\\.[\\d]{1,})|([1-9]{1}[0-9]{1,}\\.[\\d]{1,})|([\\-][1-9]{1}[0-9]{1,})|([1-9]{1}[0-9]{1,})", "g");
                    var numberDotExp = new RegExp("\\d{1}\\.{1}\\d{1}", "g");

                    var add = (this[i].text.match(numberDotExp) != null ? this[i].text.match(numberDotExp).length : 0);
                    var pos = REC.mergeStr(this[i], ".").replace(",", ".").search(numberExp);
                    if (pos != -1) {
                        this[i].setStart(this[i].getStart() + pos);
                        this[i].setEnd(this[i].getStart() + REC.mergeStr(this[i], ".").replace(",", ".").match(numberExp)[0].length + add);
                    }
                }*/
            }
        }
    }
};

/**
 * speichert das Ergebnis einer Suche
 * @param  text       der Text mit der Fundstelle
 * @param  val        das Ergebnis als passender Objecttyp
 * @param  startPos   die Beginnposition des Ergebnis im Text
 * @param  endPos     die Endeposition des Ergebnis im Text
 * @param  typ        der Typ des Ergebnis
 * @param  expected   für Testzwecke. Hier kann ein erwartetes Ergebnis hinterlegt werden
 */
function SearchResult(document, text, val, startPos, endPos, typ, expected) {
    this.document = document;
    this.text = text;
    var start = startPos;
    var end = endPos;
    this.check = true;
    this.error = null;
    this.val = val;
    this.expected = expected;
    var removedChar = [];
    // damit wird der Zugriff von privaten Methoden auf public Variabeln ermöglicht
    var that = this;
    if (!REC.exist(typ) && REC.exist(val)) {
        if (typeof val == "number")
            this.typ = "float";
        else if (val instanceof Date)
            this.typ = "date";
        else
            this.typ = "string";
    } else
        this.typ = typ;

    var calculateStartPos = function () {
        removedChar.sort(function (a, b) {
            return a - b;
        });
        var finalPos = start;
        for (var k = 0; k < removedChar.length; k++) {
            if (removedChar[k] == finalPos)
                finalPos++;
            else
                break;
        }
        start = finalPos;
    };

    var calculateEndPos = function () {
        removedChar.sort(function (a, b) {
            return a - b;
        });
        var finalPos = start + that.text.length;
        for (var k = 0; k < removedChar.length; k++) {
            if (removedChar[k] >= start) {
                if (removedChar[k] < finalPos)
                    finalPos++;
                else
                    break;
            }
        }
        end = finalPos;
    };


    this.getValue = function () {
        return this.val;
    };

    this.getEnd = function() {
        return end;
    };


    this.setEnd = function(value) {
        end = value;
    };

    this.getStart = function() {
        return start;
    };

    this.setStart = function(value) {
        start = value;
    };

    /**
     * konvertiert einen Wert in den vorgegebenen Typ
     */
    this.convertValue = function () {
        var match, prepared;
        if (REC.trim(this.typ.toString()).toLowerCase() == "string")
            this.val = this.text;
        else if (REC.trim(this.typ.toString()).toLowerCase() == "date")
            this.val = isNaN(this.buildDate(this.text).getTime()) ? null : this.buildDate(this.text);
        else if (REC.trim(this.typ.toString()).toLowerCase() == "int") {
            prepared = this.prepareNumber(this.text);
            if (isNaN(parseInt(prepared, 10)))
                this.val = null;
            else {
                // Positionen korrigieren
                this.val = parseInt(prepared, 10);
                match = prepared.match(/[-+]?\d+/);
                start = start + match.index;
                end = start + this.val.toString().length;
            }
        }
        else if (REC.trim(this.typ.toString()).toLowerCase() == "float") {
            prepared = this.prepareNumber(this.text);
            if (isNaN(parseFloat(prepared)))
                this.val = null;
            else {
                // Positionen korrigieren
                this.val = parseFloat(prepared);
                match = prepared.match(/[-+]?\d+((.|,)\d+)?/);
                start = start + match.index;
                var pos = this.text.indexOf(' ', match.index);
                if (pos != -1)
                    end = start + pos;
                else
                    end = start + this.text.substr(match.index).length;
            }
        }
    };

    /**
     * bereitet einen String so vor, das er in einen numerischen Wert konvertiert werden kann
     * @param val           der zu konvertierende String
     * @returns {string}    der Ergebnisstring
     */
    this.prepareNumber = function (val) {
        if (val.indexOf(',') == -1 && val.split(".").length - 1 == 1)
            val = val.replace(/\./g, ',');
        val = val.replace(/\./g, '').replace(/,/g, ".");
        return val;
    };


    /**
     * versucht aus dem vorgegebenen Text ein Datum aufzubauen
     * @param text          der Text
     * @return {*}          ein Datum
     */
    this.buildDate = function (text) {
        var monate = new Array("Januar", "Februar", "M\u00e4rz", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember");
        var tmp;
        var txt;
        var i;
        var dat;
        if (!REC.isDate(dat)) {
            txt = text.replace(/ /g, '');
            for (i = 0; i < monate.length; i++) {
                var pos = txt.indexOf(monate[i]);
                if (pos != -1) {
                    var form = REC.numberFormat(i + 1, "00") + ".";
                    if (pos > 0 && txt.charAt(pos - 1) != ".")
                        form = "." + form;
                    txt = txt.replace(monate[i], form);
                    tmp = txt.split(".");
                    while (tmp.length < 3)
                        tmp.unshift("01");
                    for (var k = tmp.length; k > 0; k--) {
                        if (k > 3) {
                            REC.log(WARN, "Kein Datum " + text);
                            return null;
                        }
                        if (k == tmp.length && tmp[k - 1].length == 2)
                            tmp[k - 1] = "20" + tmp[k - 1];
                        if (k != tmp.length && tmp[k - 1].length == 1)
                            tmp[k - 1] = "0" + tmp[k - 1];
                    }
                    var help = tmp[0];
                    tmp[0] = tmp[1];
                    tmp[1] = help;
                    txt = tmp.join("/");
                    dat = new Date(txt);
                    break;
                }
            }
        }
        if (!REC.isDate(dat)) {
            var jahr = text.toString().substr(6);
            if (jahr.length == 2) {
                if (parseInt(jahr, 10) < 60)
                    jahr = "20" + jahr;
                else
                    jahr = "19" + jahr;
            }
            var mon = text.toString().slice(3, 5);
            var tag = text.toString().slice(0, 2);
            dat = new Date(jahr + "/" + mon + "/" + tag);
        }
        if (!REC.isDate(dat)) {
            dat = new Date(text);
        }
        if (!REC.isDate(dat)) {
            txt = text.toString().split("/")[0] + "/01/20" + text.toString().split("/")[1];
            dat = new Date(txt);
        }
        if (!REC.isDate(dat)) {
            txt = REC.formatNumber(REC.getPosition(monate, text.toString().split(" ")[0]) + 1, 2) + "/01/" + text.toString().split(" ")[1];
            dat = new Date(txt);
        }
        return dat;
    };

    /**
     * liefert den für das Ergebnis relevanten Teil des Textes
     * @returns {*|string}
     */
    this.getRelevantTextPart = function(){
        return this.document.substring(start, end);
    };

    /**
     * Stringrepräsentation des Objektes
     * @param ident         Einrückung
     * @return {string}     das Objekt als String
     */
    this.toString = function(ident) {
        if (!REC.exist(ident))
            ident = 0;
        ident++;
        var txt = REC.getIdent(ident) + "SearchResult:\n";
        txt = txt + REC.getIdent(ident) + "text    : " + this.text + "\n";
        txt = txt + REC.getIdent(ident) + "start   : " + start + "\n";
        txt = txt + REC.getIdent(ident) + "end     : " + end + "\n";
        txt = txt + REC.getIdent(ident) + "val     : " + this.val + "\n";
        txt = txt + REC.getIdent(ident) + "typ     : " + this.typ + "\n";
        txt = txt + REC.getIdent(ident) + "expected: " + this.expected + "\n";
        return txt;
    };

    /**
     * entfernt aus dem Ergebnis ein bestimmtes Zeichen
     * die Startposition wird um die Anzahl der entfernten zeichen verschoben
     * @param c     das zu ersetzende Zeichen
     */
    this.mergeStr = function (c) {
        var arg = [];
        for (var i = 0; i < this.text.length; i++) {
            var part = this.text.substr(i, 1);
            if (part != c)
                arg.push(part);
            else
                removedChar.push(start + i);
        }
        this.text = arg.join("");
        calculateStartPos();
        calculateEndPos();
    };

    /**
     * entfernt die Leerzeichen aus dem Ergebnis
     */
    this.makeTrim = function () {
        var startpos = 0;
        var endpos = this.text.length;
        var pos = this.text.search(/[^\s\s*]/);
        if (pos != -1)
            startpos = pos;
        pos = this.text.search(/\s\s*$/);
        if (pos != -1)
            endpos = pos;
        for (var i = 0; i < this.text.length; i++)
            if (i < startpos || i > endpos)
                removedChar.push(start + i);
        this.text = REC.trim(this.text);
        calculateStartPos();
        calculateEndPos();
    };

    /**
     * limitiert das Ergebnis auf die aktuelle Zeile
     * @see readOverReturn
     */
    this.limitToReturn = function() {
        var exp = new RegExp("[\\n\\n]");
        if (typeof this.text == "string") {
            var pos = this.text.search(exp);
            if (pos != -1) {
                this.text = this.text.substr(0, pos);
                calculateEndPos();
             }
        }
    };
}

/**
 * Baut aus dem XML ein XML-Objekt auf
 * @param ruleDocument      die XML Definition als String
 * @constructor
 */
function XMLObject(ruleDocument) {
    var attributes = ruleDocument.getAttributeNames();
    var count = attributes.length;
    for (var i = 0; i < count; i++) {
        var attribute = attributes[i];
        if (attribute.indexOf(":") == -1)
            this[attribute] = ruleDocument.getAttribute(attribute);
     }
    var tmp = [];
    // for each(elem in rule.children()) {
    var elements = ruleDocument.getElements();
    for (var k = 0; k < elements.length; k++) {
        var elem = elements[k];
        if (typeof tmp[elem.tagName] == "undefined") {
            tmp[elem.tagName] = [];
            tmp[elem.tagName].push(new XMLObject(elem));
        } else
            tmp[elem.tagName].push(new XMLObject(elem));
    }

    for (nam in tmp)
        this[nam] = tmp[nam];
}

function DebugLevel(level, text) {
    this.level = level;
    this.text = text;
}


REC = {

    getDebugLevel: function (level) {
        var ret = null;
        if (this.exist(level)) {
            if (typeof level == "string") {
                if (this.trim(level).toLowerCase() == "none")
                    ret = NONE;
                else if (this.trim(level).toLowerCase() == "error")
                    ret = ERROR;
                else if (this.trim(level).toLowerCase() == "warn")
                    ret = WARN;
                else if (this.trim(level).toLowerCase() == "informational")
                    ret = INFORMATIONAL;
                else if (this.trim(level).toLowerCase() == "debug")
                    ret = DEBUG;
                else if (this.trim(level).toLowerCase() == "trace")
                    ret = TRACE;
            } else {
                if (level == 0)
                    ret = NONE;
                else if (level == 1)
                    ret = ERROR;
                else if (level == 2)
                    ret = WARN;
                else if (level == 3)
                    ret = INFORMATIONAL;
                else if (level == 4)
                    ret = DEBUG;
                else if (level == 5)
                    ret = TRACE;
            }
        } else
            ret = ERROR;
        return ret;
    },

    print: function (obj, maxDepth, prefix) {
        var result = '';
        if (!prefix)
            prefix = '';
        if (typeof obj == "object") {
            for (var key in obj) {
                if (typeof obj[key] == 'object') {
                    if (maxDepth !== undefined && maxDepth <= 1) {
                        result += (prefix + key + '=object [max depth reached]\n');
                    } else
                        result += print(obj[key], (maxDepth) ? maxDepth - 1 : maxDepth, prefix + key + '.');
                } else {
                    if (typeof obj[key] != "function")
                        if (typeof obj[key] == "string" && obj[key].length > REC.maxDebugLength)
                            result += (prefix + key + '=' + obj[key].slice(0, REC.maxDebugLength) + '...\n');
                        else
                            result += (prefix + key + '=' + obj[key] + '\n');
                }
            }
        } else if (typeof obj == 'string' && obj.length > REC.maxDebugLength)
            result += obj.slice(0, REC.maxDebugLength) + '...\n';
        else
            result += obj + '\n';
        return result;
    },

    printTrace: function (str, left) {
        var result = "";
        if (left)
            result = "..." + str.slice((str.length - REC.maxDebugLength > 0 ? str.length - REC.maxDebugLength : 0), str.length) + "\n";
        else
            result = str.slice(0, str.length < REC.maxDebugLength ? str.length : REC.maxDebugLength) + '...\n';
        return result;
    },

    /**
     * ersetzt eine Variabel mit dem Ergebnis eines SearchItems
     * @param   str     der String mit der Variabel
     * @return  Array: [0] der ersetzte String, [1] true, wenn Ersetzung erfolgreich
     */
    replaceVar: function (str) {
        var replaced = true;
        if (str.indexOf("{") != -1) {
            replaced = false;
            if (this.exist(this.currentSearchItems)) {
                for (var i = 0; i < this.currentSearchItems.length && !replaced; i++) {
                    if (str.indexOf("{" + this.currentSearchItems[i].name + "}") != -1) {
                        var erg = this.currentSearchItems[i].resolve();
                        if (this.exist(erg)) {
                            str = str.replace(new RegExp("{" + this.currentSearchItems[i].name + "}", 'g'), erg);
                            replaced = true;
                        } else
                            str = str.replace(new RegExp("{" + this.currentSearchItems[i].name + "}", 'g'), null);

                    }
                }
            }
            if (!replaced)
                REC.errors.push("could not replace Placeholder " + str.match(/\{.+\}/g) + "!");
        }
        return [str, replaced];
    },

    completeNodePath: function(node) {
        return node.displayPath.split("/").slice(2).join("/") + "/" + node.name;
    },


    dateFormat: function (formatDate, formatString) {
        if (formatDate instanceof Date) {
            var returnStr = '';

            var replaceChars = {
                shortMonths: ['Jan', 'Feb', 'Mar', 'Apr', 'Mai', 'Jun', 'Jul', 'Aug', 'Sep', 'Okt', 'Nov', 'Dez'],
                longMonths: ['Januar', 'Februar', 'M\u00e4rz', 'April', 'Mai', 'Juni', 'Juli', 'August', 'September', 'Oktober', 'November', 'Dezember'],
                shortDays: ['Son', 'Mon', 'Die', 'Mit', 'Don', 'Fre', 'Sam'],
                longDays: ['Sonntag', 'Montag', 'Dienstag', 'Mittwoch', 'Donnerstag', 'Freitag', 'Samstag'],

                // Day
                d: function () {
                    return (formatDate.getDate() < 10 ? '0' : '') + formatDate.getDate();
                },
                D: function () {
                    return this.replaceChars.shortDays[formatDate.getDay()];
                },
                j: function () {
                    return formatDate.getDate();
                },
                l: function () {
                    return this.replaceChars.longDays[formatDate.getDay()];
                },
                N: function () {
                    return formatDate.getDay() + 1;
                },
                S: function () {
                    return (formatDate.getDate() % 10 == 1 && formatDate.getDate() != 11 ? 'st' : (formatDate.getDate() % 10 == 2 && formatDate.getDate() != 12 ? 'nd'
                        : (formatDate.getDate() % 10 == 3 && formatDate.getDate() != 13 ? 'rd' : 'th')));
                },
                w: function () {
                    return formatDate.getDay();
                },
                z: function () {
                    var d = new Date(formatDate.getFullYear(), 0, 1);
                    return Math.ceil((formatDate - d) / 86400000);
                },
                // Fixed now
                // Week
                W: function () {
                    var d = new Date(formatDate.getFullYear(), 0, 1);
                    return Math.ceil((((formatDate - d) / 86400000) + d.getDay() + 1) / 7);
                },
                // Fixed now
                // Month
                F: function () {
                    return replaceChars.longMonths[formatDate.getMonth()];
                },
                m: function () {
                    return (formatDate.getMonth() < 9 ? '0' : '') + (formatDate.getMonth() + 1);
                },
                M: function () {
                    return replaceChars.shortMonths[formatDate.getMonth()];
                },
                n: function () {
                    return formatDate.getMonth() + 1;
                },
                t: function () {
                    var d = new Date();
                    return new Date(d.getFullYear(), d.getMonth(), 0).getDate()
                },
                // Fixed now, gets #days of date
                // Year
                L: function () {
                    var year = formatDate.getFullYear();
                    return (year % 400 == 0 || (year % 100 != 0 && year % 4 == 0));
                },
                // Fixed now
                o: function () {
                    var d = new Date(formatDate.valueOf());
                    d.setDate(d.getDate() - ((formatDate.getDay() + 6) % 7) + 3);
                    return d.getFullYear();
                },
                // Fixed now
                Y: function () {
                    return formatDate.getFullYear();
                },
                y: function () {
                    return ('' + formatDate.getFullYear()).substr(2);
                },
                // Time
                a: function () {
                    return formatDate.getHours() < 12 ? 'am' : 'pm';
                },
                A: function () {
                    return formatDate.getHours() < 12 ? 'AM' : 'PM';
                },
                B: function () {
                    return Math.floor((((formatDate.getUTCHours() + 1) % 24) + formatDate.getUTCMinutes() / 60 + formatDate.getUTCSeconds() / 3600) * 1000 / 24);
                },
                // Fixed now
                g: function () {
                    return formatDate.getHours() % 12 || 12;
                },
                G: function () {
                    return formatDate.getHours();
                },
                h: function () {
                    return ((formatDate.getHours() % 12 || 12) < 10 ? '0' : '') + (formatDate.getHours() % 12 || 12);
                },
                H: function () {
                    return (formatDate.getHours() < 10 ? '0' : '') + formatDate.getHours();
                },
                i: function () {
                    return (formatDate.getMinutes() < 10 ? '0' : '') + formatDate.getMinutes();
                },
                s: function () {
                    return (formatDate.getSeconds() < 10 ? '0' : '') + formatDate.getSeconds();
                },
                u: function () {
                    var m = formatDate.getMilliseconds();
                    return (m < 10 ? '00' : (m < 100 ? '0' : '')) + m;
                },
                // Timezone
                e: function () {
                    return "Not Yet Supported";
                },
                I: function () {
                    return "Not Yet Supported";
                },
                O: function () {
                    return (-formatDate.getTimezoneOffset() < 0 ? '-' : '+') + (Math.abs(formatDate.getTimezoneOffset() / 60) < 10 ? '0' : '')
                        + (Math.abs(formatDate.getTimezoneOffset() / 60)) + '00';
                },
                P: function () {
                    return (-formatDate.getTimezoneOffset() < 0 ? '-' : '+') + (Math.abs(formatDate.getTimezoneOffset() / 60) < 10 ? '0' : '')
                        + (Math.abs(formatDate.getTimezoneOffset() / 60)) + ':00';
                },
                // Fixed now
                T: function () {
                    var m = formatDate.getMonth();
                    formatDate.setMonth(0);
                    var result = formatDate.toTimeString().replace(/^.+ \(?([^\)]+)\)?$/, '$1');
                    formatDate.setMonth(m);
                    return result;
                },
                Z: function () {
                    return -formatDate.getTimezoneOffset() * 60;
                },
                // Full Date/Time
                c: function () {
                    return formatDate.format("Y-m-d\\TH:i:sP");
                },
                // Fixed now
                r: function () {
                    return formatDate.toString();
                },
                U: function () {
                    return formatDate.getTime() / 1000;
                }
            };

            formatString = formatString.replace("MMMM", "F").replace("MMM", "M").replace("MM", "m").replace("YYYY", "Y").replace("YY", "y").replace("dd", "d");

            for (var i = 0; i < formatString.length; i++) {
                var curChar = formatString.charAt(i);
                if (i - 1 >= 0 && formatString.charAt(i - 1) == "\\") {
                    returnStr += curChar;
                } else if (replaceChars[curChar]) {
                    returnStr += replaceChars[curChar].call(this.formatDate);
                } else if (curChar != "\\") {
                    returnStr += curChar;
                }
            }
            return returnStr;
        } else
            return "";
    },

    /**
     * formatiert Zahlen
     * @param   formatNumber    die zu formatierende Zahl
     * @param   formatString    der Format String
     * @return  die formatierte Zahl als String
     * */
    numberFormat: function (formatNumber, formatString) {
        var c, d, e, f, g, h, i, j, k;
        if (!formatString || isNaN(+formatNumber))
            return formatNumber;
        formatNumber = formatString.charAt(0) == "-" ? -formatNumber : +formatNumber, j = formatNumber < 0 ? formatNumber = -formatNumber : 0, e = formatString.match(/[^\d\-\+#]/g),
            h = e && e[e.length - 1] || ".", e = e && e[1] && e[0] || ",", formatString = formatString.split(h), formatNumber = formatNumber.toFixed(formatString[1]
        && formatString[1].length), formatNumber = +formatNumber + "", d = formatString[1] && formatString[1].lastIndexOf("0"), c = formatNumber.split(".");
        if (!c[1] || c[1] && c[1].length <= d)
            formatNumber = (+formatNumber).toFixed(d + 1);
        d = formatString[0].split(e);
        formatString[0] = d.join("");
        f = formatString[0] && formatString[0].indexOf("0");
        if (f > -1)
            for (; c[0].length < formatString[0].length - f;)
                c[0] = "0" + c[0];
        else
            +c[0] == 0 && (c[0] = "");
        formatNumber = formatNumber.split(".");
        formatNumber[0] = c[0];
        if (c = d[1] && d[d.length - 1].length) {
            for ( d = formatNumber[0], f = "", k = d.length % c, g = 0, i = d.length; g < i; g++)
                f += d.charAt(g), !((g - k + 1) % c) && g < i - c && (f += e);
            formatNumber[0] = f;
        }
        formatNumber[1] = formatString[1] && formatNumber[1] ? h + formatNumber[1] : "";
        return (j ? "-" : "") + formatNumber[0] + formatNumber[1];
    },

    monatName: function (datum) {
        var monatZahl = datum.getMonth();
        return monat[monatZahl];
    },

    fillValues: function (value, srch) {
        if (this.exist(value)) {
            if (this.exist(srch.archivZiel[0].aspect)) {
                if (!this.currentDocument.hasAspect(srch.archivZiel[0].aspect.toString()))
                    this.currentDocument.addAspect(srch.archivZiel[0].aspect.toString());
                REC.log(INFORMATIONAL, "Document add aspect " + srch.archivZiel[0].aspect.toString());
            }
            this.log(DEBUG, srch.name + ": " + value);
            this.log(INFORMATIONAL, "Document.properties[\"" + srch.archivZiel[0].target + "\"] = \"" + value + "\"");
            this.currentDocument.properties[srch.archivZiel[0].target] = value;
        }
    },

    isEmpty: function (str) {
        return (str == null) || (str.length == 0);
    },

    isDate: function (x) {
        return (null != x) && !isNaN(x) && ("undefined" !== typeof x.getDate);
    },

    getAmountInEuro: function (text, date) {
        var x = parseFloat(text);
        if (date < new Date("August 01, 2001 00:00:00"))
            x = dmToEuro(x);
        return x;
    },

    dmToEuro: function (x) {
        var k = (Math.round((x / 1.95583) * 100) / 100).toString();
        k += (k.indexOf('.') == -1) ? '.00' : '00';
        return parseFloat(k.substring(0, k.indexOf('.') + 3));
    },

    isNumeric: function (n) {
        var n2 = n;
        n = parseFloat(n);
        return (!isNaN(n) && n2 == n);
    },
    trim: function (str) {
        return str.replace(/^\s\s*/, '').replace(/\s\s*$/, '');
    },

    trim: function (trimString, leftTrim, rightTrim) {
        if (REC.isEmpty(trimString)) {
            return "";
        }
        if (leftTrim == null) {
            leftTrim = true;
        }
        if (rightTrim == null) {
            rightTrim = true;
        }
        var left = 0;
        var right = 0;
        var i = 0;
        var k = 0;
        if (leftTrim == true) {
            while ((i < trimString.length) && (whitespace.indexOf(trimString.charAt(i++)) != -1)) {
                left++;
            }
        }
        if (rightTrim == true) {
            k = trimString.length - 1;
            while ((k >= left) && (whitespace.indexOf(trimString.charAt(k--)) != -1)) {
                right++;
            }
        }
        return trimString.substring(left, trimString.length - right);
    },

    convertEscapes: function (str) {
        var escAmpRegEx = /&amp;/g;
        var escLtRegEx = /&lt;/g;
        var escGtRegEx = /&gt;/g;
        str = str.replace(escAmpRegEx, "&");
        str = str.replace(escLtRegEx, "<");
        str = str.replace(escGtRegEx, ">");
        return str;
    },


    convertToEscapes: function (str) {
        var escAmpRegEx = /&/g;
        var escLtRegEx = /</g;
        var escGtRegEx = />/g;
        str = str.replace(escAmpRegEx, "&amp;");
        str = str.replace(escLtRegEx, "&lt;");
        str = str.replace(escGtRegEx, "&gt;");
        return str;
    },

    firstWhiteChar: function (str, pos) {
        if (REC.isEmpty(str)) {
            return -1;
        }
        while (pos < str.length) {
            if (whitespace.indexOf(str.charAt(pos)) != -1) {
                return pos;
            } else {
                pos++;
            }
        }
        return str.length;
    },
    displayElement: function (domElement, strRet) {
        var intLoop;
        if (domElement == null) {
            return;
        }
        if (!(domElement.nodeType == 'ELEMENT')) {
            return;
        }
        var tagName = domElement.tagName;
        var tagInfo = "";
        tagInfo = "<" + tagName;
        var attributeList = domElement.getAttributeNames();
        for (intLoop = 0; intLoop < attributeList.length; intLoop++) {
            var attribute = attributeList[intLoop];
            tagInfo = tagInfo + " " + attribute + "=";
            tagInfo = tagInfo + "\"" + domElement.getAttribute(attribute) + "\"";
        }
        tagInfo = tagInfo + ">";
        strRet = strRet + tagInfo;
        if (domElement.children != null) {
            var cont;
            var domElements = domElement.children;
            for (intLoop = 0; intLoop < domElements.length; intLoop++) {
                var childNode = domElements[intLoop];
                if (childNode.nodeType == 'COMMENT') {
                    strRet = strRet + "<!--" + childNode.content + "-->";
                }
                else if (childNode.nodeType == 'TEXT') {
                    cont = REC.trim(childNode.content, true, true);
                    strRet = strRet + childNode.content;
                }
                else if (childNode.nodeType == 'CDATA') {
                    cont = REC.trim(childNode.content, true, true);
                    strRet = strRet + "<![CDATA[" + cont + "]]>";
                }
                else {
                    strRet = REC.displayElement(childNode, strRet);
                }
            }
        }
        strRet = strRet + "</" + tagName + ">";
        return strRet;
    },

    /**
     * liefert die angesammelten Meldungen
     * @param reverse       die Meldungen werden in umgekehrter Reihenfolge ausgegeben
     * @return {string}
     */
    getMessage: function (reverse) {
        var output;
        var ident = "                    ";
        var out = [];
        var messages = [];
        var pos = 0;
        if (this.errors.length > 0) {
            for (var i = 0; i < this.errors.length; i++)
                this.log(ERROR, "Fehler: " + this.errors[i]);
        }
/*        if (this.showContent || this.debugLevel.level >= DEBUG.level) {
            this.mess.push("=====>");
            this.mess.push(this.content);
            this.mess.push("<====");
        }*/
        for (var j = 0; j < this.mess.length; j++) {
            var zeile = this.mess[j];
            var z = zeile.split("\n");
            var i = 0;
            for (var k = 0; k < z.length; k++) {
                var z1 = z[k];
                if (i == 0 && z1.length > 0) {
                    pos = z1.indexOf(" ", z1.indexOf(" ") + 1) + 1;
                    out.push(z1);
                    i++;
                } else if (z1.length > 0)
                    out.push(ident.substr(0, pos) + z1);
            }
            messages.push(out.join("\n"));
            out = [];
        }
        if (reverse) {
            output = messages.reverse().join("\n");
        } else {
            output = messages.join("\n");
        }
        return output;
    },

    log: function (level, text) {
        if (this.debugLevel.level >= level.level) {
            this.mess.push(this.dateFormat(new Date(), "G:i:s,u") + " " + level.text + " " + text);
        }
    },

    /**
     * prüft, ob eine Wert existiert
     * @param val           der zu prüfende Wert
     * @return {boolean}    [true]    wenn der Wert existiert
     *                      [false]   wenn der Wert nicht existiert
     */
    exist: function (val) {
        return typeof val != "undefined" && val != null;
    },
    searchArray: function (arr, value) {
        var arr2str = arr.toString();
        return arr2str.search(value);
    },

    getPosition: function (arr, obj) {
        for (var i = 0; i < arr.length; i++) {
            if (arr[i] == obj)
                return i;
        }
        return -1;
    },

    formatNumber: function (zahl, laenge) {
        var erg = String(zahl);
        while (laenge > erg.length)
            erg = "0" + erg;
        return erg;
    },
    stringToBoolean: function (string, defaultVal) {
        if (this.exist(string)) {
            switch (string.toLowerCase()) {
                case "true":
                case "yes":
                case "1":
                    return true;
                case "false":
                case "no":
                case "0":
                case null:
                    return false;
                default:
                    return Boolean(string);
            }
        } else
            return defaultVal;
    },

    getRules: function () {
        var rules = script.parent.childByNamePath("doc.xml");
        if (!this.exist(rules)) {
            throw "Regeln nicht gefunden";
        }
        this.log(INFORMATIONAL, "Regeln gefunden!");
        this.log(TRACE, "Lade XML...");
        XMLDoc.loadXML(rules.content + "");
        XMLDoc.parse();
        this.log(TRACE, "XML geladen");
        return new XMLObject(XMLDoc.docNode);
    },
    getIdent: function (count) {
        var ret = "";
        for (var i = 0; i < count; i++)
            ret = ret + "\t";
        return ret;
    },


    getContent: function (doc) {
        var erg;
        var trans = doc.transformDocument("text/plain");
        if (this.exist(trans)) {
            erg = trans.content + "";
            trans.remove();
            if (!this.exist(erg) || erg.length == 0) {
                throw "Dokumenteninhalt konnte nicht gefunden werden";
            }
        } else {
            throw "Dokumenteninhalt konnte nicht extrahiert werden";
        }
        return erg.replace(/\r\n/g,'\n');
    },

    /**
     * konvertiert die absoluten Positionsangaben in ein Positionsobject mit Zeilen und Spalten
     * @param text          der Text
     * @param start         die absolute Startposition
     * @param end           die absolute Endposition
     * @param desc          die Beschreibung der Positionsangabe
     * @param type          der Typ der Positionsangabe
     * @returns {Position}  das konvertierte Positionsobject
     */
    convertPosition: function (text, start, end, desc, type) {
        var startRow = text.substring(0, start).split("\n").length - 1;
        var startCol = start - text.substring(0, start).lastIndexOf("\n") - 1;
        var endRow = text.substring(0, end).split("\n").length - 1;
        var endCol = end - text.substring(0, end).lastIndexOf("\n") - 1;
        return new Position(startRow, startCol, endRow, endCol, type, desc);
    },

    handleUnexpected: function (box) {
        if (this.errors.length > 0) {
            var comment = "<table border=\"1\"> <tr><td>Nummer</td><td>Fehler</td></tr> ";
            for (var i = 0; i < this.errors.length; i++) {
                comment = comment + "<tr>";
                comment = comment + "<td>" + (i + 1) + "</td>";
                comment = comment + "<td>" + this.errors[i] + "</td>";
                comment = comment + "</tr>";
            }
            comment = comment + "</table>";
            this.log(TRACE, "adding Comment " + comment);
            var COM = new Comments();
            COM.addComment(this.currentDocument, comment);
            var archivPosition = new ArchivPosition({
                folder: box,
                link: "false",
                resolve: ArchivPosition.resolve
            });
            var archivTyp = new ArchivTyp({
                searchString: "",
                resolve: ArchivTyp.resolve,
                name: "Fehler",
                archivPosition: archivPosition
            });
            archivTyp.resolve("");
        }
    },
    testRules: function (rules) {
        try {
            this.currXMLName = [];
            XMLDoc.loadXML(rules);
            XMLDoc.parse();
            this.recognize(this.currentDocument, new XMLObject(XMLDoc.docNode));
        } catch (e) {
            this.log(ERROR, e.toString());
            var str = "";
            for (var prop in e)
                str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
            this.log(ERROR, "Stacktrace:\n" + e.stack);
        } finally {
            this.handleUnexpected(this.fehlerBox);
        }
    },

    /**
     * führt die Erkennung durch
     * @param doc       das zu erkennende Dokument
     * @param rules     die Regeln
     * @param deb       wird nicht benutzt
     */
    recognize: function (doc, rules, deb) {
        if (this.exist(rules.debugLevel))
            this.debugLevel = this.getDebugLevel(rules.debugLevel);
        this.log(INFORMATIONAL, "Debug Level is set to: " + this.print(this.debugLevel));
        if (this.exist(rules.maxDebugLength))
            this.maxDebugLength = parseInt(rules.maxDebugLength, 10);
        else
            this.maxDebugLength = 80;
        this.log(INFORMATIONAL, "Debug length is set to: " + this.maxDebugLength);
        this.currentDocument = doc;
        var docName = this.currentDocument.name;
        this.log(INFORMATIONAL, "Process Dokument " + docName);
        this.content = this.getContent(this.currentDocument);
        if (this.exist(rules.archivRoot))
            this.archivRoot = companyhome.childByNamePath(this.trim(rules.archivRoot));
        else
            this.archivRoot = companyhome.childByNamePath("Archiv");
        if (this.exist(this.archivRoot))
            this.log(INFORMATIONAL, "ArchivRoot is located: " + this.completeNodePath(this.archivRoot));
        else
            throw "Archiv Root not found!";
        if (this.exist(rules.inBox))
            this.inBox = this.archivRoot.childByNamePath(this.trim(rules.inBox));
        else
            this.inBox = this.archivRoot.childByNamePath("Inbox");
        if (this.exist(this.inBox))
            this.log(INFORMATIONAL, "Inbox is located: " + this.completeNodePath(this.inBox));
        else
            throw "Inbox not found!";
        if (this.exist(rules.errorBox))
            this.errorBox = this.archivRoot.childByNamePath(this.trim(rules.errorBox));
        else
            this.errorBox = this.archivRoot.childByNamePath("Fehler");
        if (this.exist(this.errorBox))
            this.log(INFORMATIONAL, "ErrorBox is located: " + this.completeNodePath(this.errorBox));
        else
            throw "ErrorBox not found!";
        if (this.exist(rules.duplicateBox))
            this.duplicateBox = this.archivRoot.childByNamePath(this.trim(rules.duplicateBox));
        else
            this.duplicateBox = this.archivRoot.childByNamePath("Fehler/Doppelte");
        if (this.exist(this.duplicateBox))
            this.log(INFORMATIONAL, "DuplicateBox is located: " + this.completeNodePath(this.duplicateBox));
        else
            throw "DuplicateBox not found!";
        if (this.exist(rules.unknownBox))
            this.unknownBox = this.archivRoot.childByNamePath(this.trim(rules.unknownBox));
        else
            this.unknownBox = this.archivRoot.childByNamePath("Unbekannt");
        if (this.exist(this.unknownBox))
            this.log(INFORMATIONAL, "UnknownBox is located: " + this.completeNodePath(this.unknownBox));
        else
            throw "UnknownBox not found!";
        if (this.exist(rules.mandatory)) {
            var mnd = this.trim(rules.mandatory);
            this.mandatoryElements = mnd.split(",");
            this.log(INFORMATIONAL, "Mandatory Elements are: " + mnd);
        }
        this.fehlerBox = this.errorBox;
        var ruleFound = false;
        this.currentSearchItems = null;
        for (var i = 0; i < rules.archivTyp.length; i++) {
            ruleFound = new ArchivTyp(rules.archivTyp[i]).resolve();
            if (ruleFound)
                break;
        }
        if (!ruleFound) {
            this.errors.push("Unbekanntes Dokument, keine passende Regel gefunden!");
            if (!doc.move(this.unknownBox))
               REC.errors.push("Dokument konnte nicht in den Zielordner verschoben werden " + REC.completeNodePath(REC.unknownBox));
        }
        this.log(INFORMATIONAL, "Process Dokument " + docName + " finished!");
    },

    run: function () {
        if (typeof (space) != "undefined") {
            try {
                this.recognize(document, this.getRules(), space.name != "Inbox");
            } catch (e) {
                var str = e.toString() + "\n";
                for (var prop in e)
                    str = str + "property: " + prop + " value: [" + e[prop] + "]\n";
                str = str + "Stacktrace: \n" + e.stack.split('\n').reverse().join('\n');
                this.log(ERROR, str);
                this.errors.push("Fehler: " + e.toString());
            } finally {
                this.handleUnexpected(this.fehlerBox);
                logger.log(this.getMessage(false));
            }
        }
    },

    currentDocument: null,

    init: function(){
        this.id = Math.random() * 100;
        this.debugLevel = INFORMATIONAL;
        this.mess = [];
        this.content = "";
        this.fehlerBox = null;
        this.maxDebugLength = 0;
        this.mandatoryElements = [];
        this.currentSearchItems = [];
        this.currXMLName = [];
        this.showContent = false;
        this.result = [];
        this.errors = [];
        this.results = [];
        this.positions = new PositionContainer();
        companyhome.init();
        this.archivRoot = companyhome.createFolder("Archiv");
        this.unknownBox = this.archivRoot.createFolder("Unbekannt");
        this.inBox = this.archivRoot.createFolder("Inbox");
        this.errorBox  = this.archivRoot.createFolder("Fehler");
        this.duplicateBox = this.errorBox.createFolder("Doppelte");
        this.currentDocument = companyhome.createNode('WebScriptTest', "my:archivContent");
    },
    
    id: Math.random() * 100,
    debugLevel: DEBUG,
    mess: [],
    content: "",
    archivRoot: null,
    inBox: null,
    duplicateBox: null,
    errorBox: null,
    unknownBox: null,
    fehlerBox: null,
    maxDebugLength: 0,
    mandatoryElements: [],
    currentSearchItems: [],
    currXMLName: [],
    showContent: false,
    result: [],
    errors: [],
    results: [],
    positions: new PositionContainer()
};
REC.run();

