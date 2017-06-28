/**
 * Created by m500288 on 6/26/17.
 */

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
        throw "Element " + element.name + " already available";
    this.push(element);
};
Liste.prototype.remove = function(element) {
    if (!this.contains(element))
        throw "Element " + element.name + " not available";
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
        throw "no folder!";
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
        throw "Node " + this.name + " is already checked out!";
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
        throw "Node " + this.name + " is not checked out!";
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
        throw this.name + " has no version!";
    if (!this.versions.contains(new BasicObject(label)))
        throw "Version " + label + " of " +this.name + " is not available!";
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
        throw "No Folder!";
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
        throw "No Folder!";
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

ScriptNode.prototype.removeProperty = function(key){
    this.properties.remove(key);
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
        throw "Root Category " + name + " is already avaiable!";
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
