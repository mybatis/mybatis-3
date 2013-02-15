var API_LEVEL_ENABLED_COOKIE = "api_level_enabled";
var API_LEVEL_INDEX_COOKIE = "api_level_index";
var minLevelIndex = 0;

function toggleApiLevelSelector(checkbox) {
  var date = new Date();
  date.setTime(date.getTime()+(10*365*24*60*60*1000)); // keep this for 10 years
  var expiration = date.toGMTString();
  if (checkbox.checked) {
    $("#apiLevelSelector").removeAttr("disabled");
    $("#api-level-toggle label").removeClass("disabled");
    writeCookie(API_LEVEL_ENABLED_COOKIE, 1, null, expiration);
  } else {
    $("#apiLevelSelector").attr("disabled","disabled");
    $("#api-level-toggle label").addClass("disabled");
    writeCookie(API_LEVEL_ENABLED_COOKIE, 0, null, expiration);
  }
  changeApiLevel();
}

function buildApiLevelSelector() {
  var userApiLevelEnabled = readCookie(API_LEVEL_ENABLED_COOKIE);
  var userApiLevelIndex = readCookie(API_LEVEL_INDEX_COOKIE); // No cookie (zero) is the same as maxLevel.
  
  if (userApiLevelEnabled == 0) {
    $("#apiLevelSelector").attr("disabled","disabled");
  } else {
    $("#apiLevelCheckbox").attr("checked","checked");
    $("#api-level-toggle label").removeClass("disabled");
  }
  
  minLevelValue = $("body").attr("class");
  minLevelIndex = apiKeyToIndex(minLevelValue);
  var select = $("#apiLevelSelector").html("").change(changeApiLevel);
  for (var i = SINCE_DATA.length-1; i >= 0; i--) {
    var option = $("<option />").attr("value",""+SINCE_DATA[i]).append(""+SINCE_LABELS[i]);
    select.append(option);
  }
  
  // get the DOM element and use setAttribute cuz IE6 fails when using jquery .attr('selected',true)
  var selectedLevelItem = $("#apiLevelSelector option").get(SINCE_DATA.length - userApiLevelIndex - 1);
  selectedLevelItem.setAttribute('selected',true);
}

function changeApiLevel() {
  var userApiLevelEnabled = readCookie(API_LEVEL_ENABLED_COOKIE);
  var selectedLevelIndex = SINCE_DATA.length - 1;
  
  if (userApiLevelEnabled == 0) {
    toggleVisisbleApis(selectedLevelIndex, "body");
  } else {
    selectedLevelIndex = getSelectedLevelIndex();
    toggleVisisbleApis(selectedLevelIndex, "body");
    
    var date = new Date();
    date.setTime(date.getTime()+(10*365*24*60*60*1000)); // keep this for 10 years
    var expiration = date.toGMTString();
    writeCookie(API_LEVEL_INDEX_COOKIE, selectedLevelIndex, null, expiration);
  }
  
  var thing = ($("#jd-header").html().indexOf("package") != -1) ? "package" : "class";
  showApiWarning(thing, selectedLevelIndex, minLevelIndex);
}

function showApiWarning(thing, selectedLevelIndex, minLevelIndex) {
  if (selectedLevelIndex < minLevelIndex) {
	  $("#naMessage").show().html("<div><p><strong>This " + thing
		  + " is not available with API version "
		  + SINCE_LABELS[selectedLevelIndex] + ".</strong></p>"
	      + "<p>To reveal this "
	      + "document, change the value in the API filter above.</p>");
  } else {
    $("#naMessage").hide();
  }
}

function toggleVisisbleApis(selectedLevelIndex, context) {
  var apis = $(".api",context);
  apis.each(function(i) {
    var obj = $(this);
    var className = obj.attr("class");
    var apiLevelPos = className.lastIndexOf("-")+1;
    var apiLevelEndPos = className.indexOf(" ", apiLevelPos);
    apiLevelEndPos = apiLevelEndPos != -1 ? apiLevelEndPos : className.length;
    var apiLevelName = className.substring(apiLevelPos, apiLevelEndPos);
    var apiLevelIndex = apiKeyToIndex(apiLevelName);
    if (apiLevelIndex > selectedLevelIndex) {
      obj.addClass("absent").attr("title","Requires API Level "+SINCE_LABELS[apiLevelIndex]+" or higher");
    } else {
      obj.removeClass("absent").removeAttr("title");
    }
  });
}

function apiKeyToIndex(key) {
  for (i = 0; i < SINCE_DATA.length; i++) {
    if (SINCE_DATA[i] == key) {
      return i;
    }
  }
  return -1;
}

function getSelectedLevelIndex() {
  return SINCE_DATA.length - $("#apiLevelSelector").attr("selectedIndex") - 1;
}

/* NAVTREE */

function new_node(me, mom, text, link, children_data, api_level)
{
  var node = new Object();
  node.children = Array();
  node.children_data = children_data;
  node.depth = mom.depth + 1;

  node.li = document.createElement("li");
  mom.get_children_ul().appendChild(node.li);

  node.label_div = document.createElement("div");
  node.label_div.className = "label";
  if (api_level != null) {
    $(node.label_div).addClass("api");
    $(node.label_div).addClass("api-level-"+api_level);
  }
  node.li.appendChild(node.label_div);
  node.label_div.style.paddingLeft = 10*node.depth + "px";

  if (children_data == null) {
    // 12 is the width of the triangle and padding extra space
    node.label_div.style.paddingLeft = ((10*node.depth)+12) + "px";
  } else {
    node.label_div.style.paddingLeft = 10*node.depth + "px";
    node.expand_toggle = document.createElement("a");
    node.expand_toggle.href = "javascript:void(0)";
    node.expand_toggle.onclick = function() {
          if (node.expanded) {
            $(node.get_children_ul()).slideUp("fast");
            node.plus_img.src = toAssets + "images/triangle-closed-small.png";
            node.expanded = false;
          } else {
            expand_node(me, node);
          }
       };
    node.label_div.appendChild(node.expand_toggle);

    node.plus_img = document.createElement("img");
    node.plus_img.src = toAssets + "images/triangle-closed-small.png";
    node.plus_img.className = "plus";
    node.plus_img.border = "0";
    node.expand_toggle.appendChild(node.plus_img);

    node.expanded = false;
  }

  var a = document.createElement("a");
  node.label_div.appendChild(a);
  node.label = document.createTextNode(text);
  a.appendChild(node.label);
  if (link) {
    a.href = me.toroot + link;
  } else {
    if (children_data != null) {
      a.className = "nolink";
      a.href = "javascript:void(0)";
      a.onclick = node.expand_toggle.onclick;
      // This next line shouldn't be necessary.
      node.expanded = false;
    }
  }
  

  node.children_ul = null;
  node.get_children_ul = function() {
      if (!node.children_ul) {
        node.children_ul = document.createElement("ul");
        node.children_ul.className = "children_ul";
        node.children_ul.style.display = "none";
        node.li.appendChild(node.children_ul);
      }
      return node.children_ul;
    };

  return node;
}

function expand_node(me, node)
{
  if (node.children_data && !node.expanded) {
    if (node.children_visited) {
      $(node.get_children_ul()).slideDown("fast");
    } else {
      get_node(me, node);
      if ($(node.label_div).hasClass("absent")) $(node.get_children_ul()).addClass("absent");
      $(node.get_children_ul()).slideDown("fast");
    }
    node.plus_img.src = toAssets + "images/triangle-opened-small.png";
    node.expanded = true;
    
    // perform api level toggling because new nodes are new to the DOM
    var selectedLevel = $("#apiLevelSelector").attr("selectedIndex");
    toggleVisisbleApis(selectedLevel, "#side-nav");
  }
}

function get_node(me, mom)
{
  mom.children_visited = true;
  for (var i in mom.children_data) {
    var node_data = mom.children_data[i];
    mom.children[i] = new_node(me, mom, node_data[0], node_data[1],
        node_data[2], node_data[3]);
  }
}

function this_page_relative(toroot)
{
  var full = document.location.pathname;
  var file = "";
  if (toroot.substr(0, 1) == "/") {
    if (full.substr(0, toroot.length) == toroot) {
      return full.substr(toroot.length);
    } else {
      // the file isn't under toroot.  Fail.
      return null;
    }
  } else {
    if (toroot != "./") {
      toroot = "./" + toroot;
    }
    do {
      if (toroot.substr(toroot.length-3, 3) == "../" || toroot == "./") {
        var pos = full.lastIndexOf("/");
        file = full.substr(pos) + file;
        full = full.substr(0, pos);
        toroot = toroot.substr(0, toroot.length-3);
      }
    } while (toroot != "" && toroot != "/");
    return file.substr(1);
  }
}

function find_page(url, data)
{
  var nodes = data;
  var result = null;
  for (var i in nodes) {
    var d = nodes[i];
    if (d[1] == url) {
      return new Array(i);
    }
    else if (d[2] != null) {
      result = find_page(url, d[2]);
      if (result != null) {
        return (new Array(i).concat(result));
      }
    }
  }
  return null;
}

function load_navtree_data() {
  var navtreeData = document.createElement("script");
  navtreeData.setAttribute("type","text/javascript");
  navtreeData.setAttribute("src", toAssets + "navtree_data.js");
  $("head").append($(navtreeData));
}

function init_default_navtree(toroot) {
  init_navtree("nav-tree", toroot, NAVTREE_DATA);
  
  // perform api level toggling because because the whole tree is new to the DOM
  var selectedLevel = $("#apiLevelSelector").attr("selectedIndex");
  toggleVisisbleApis(selectedLevel, "#side-nav");
}

function init_navtree(navtree_id, toroot, root_nodes)
{
  var me = new Object();
  me.toroot = toroot;
  me.node = new Object();

  me.node.li = document.getElementById(navtree_id);
  me.node.children_data = root_nodes;
  me.node.children = new Array();
  me.node.children_ul = document.createElement("ul");
  me.node.get_children_ul = function() { return me.node.children_ul; };
  //me.node.children_ul.className = "children_ul";
  me.node.li.appendChild(me.node.children_ul);
  me.node.depth = 0;

  get_node(me, me.node);

  me.this_page = this_page_relative(toroot);
  me.breadcrumbs = find_page(me.this_page, root_nodes);
  if (me.breadcrumbs != null && me.breadcrumbs.length != 0) {
    var mom = me.node;
    for (var i in me.breadcrumbs) {
      var j = me.breadcrumbs[i];
      mom = mom.children[j];
      expand_node(me, mom);
    }
    mom.label_div.className = mom.label_div.className + " selected";
    addLoadEvent(function() {
      scrollIntoView("nav-tree");
      });
  }
}

/* TOGGLE INHERITED MEMBERS */

/* Toggle an inherited class (arrow toggle)
 * @param linkObj  The link that was clicked.
 * @param expand  'true' to ensure it's expanded. 'false' to ensure it's closed.
 *                'null' to simply toggle.
 */
function toggleInherited(linkObj, expand) {
    var base = linkObj.getAttribute("id");
    var list = document.getElementById(base + "-list");
    var summary = document.getElementById(base + "-summary");
    var trigger = document.getElementById(base + "-trigger");
    var a = $(linkObj);
    if ( (expand == null && a.hasClass("closed")) || expand ) {
        list.style.display = "none";
        summary.style.display = "block";
        trigger.src = toAssets + "images/triangle-opened.png";
        a.removeClass("closed");
        a.addClass("opened");
    } else if ( (expand == null && a.hasClass("opened")) || (expand == false) ) {
        list.style.display = "block";
        summary.style.display = "none";
        trigger.src = toAssets + "images/triangle-closed.png";
        a.removeClass("opened");
        a.addClass("closed");
    }
    return false;
}

/* Toggle all inherited classes in a single table (e.g. all inherited methods)
 * @param linkObj  The link that was clicked.
 * @param expand  'true' to ensure it's expanded. 'false' to ensure it's closed.
 *                'null' to simply toggle.
 */
function toggleAllInherited(linkObj, expand) {
  var a = $(linkObj);
  var table = $(a.parent().parent().parent()); // ugly way to get table/tbody
  var expandos = $(".jd-expando-trigger", table);
  if ( (expand == null && a.text() == "[Expand]") || expand ) {
    expandos.each(function(i) {
      toggleInherited(this, true);
    });
    a.text("[Collapse]");
  } else if ( (expand == null && a.text() == "[Collapse]") || (expand == false) ) {
    expandos.each(function(i) {
      toggleInherited(this, false);
    });
    a.text("[Expand]");
  }
  return false;
}

/* Toggle all inherited members in the class (link in the class title)
 */
function toggleAllClassInherited() {
  var a = $("#toggleAllClassInherited"); // get toggle link from class title
  var toggles = $(".toggle-all", $("#doc-content"));
  if (a.text() == "[Expand All]") {
    toggles.each(function(i) {
      toggleAllInherited(this, true);
    });
    a.text("[Collapse All]");
  } else {
    toggles.each(function(i) {
      toggleAllInherited(this, false);
    });
    a.text("[Expand All]");
  }
  return false;
}

/* Expand all inherited members in the class. Used when initiating page search */
function ensureAllInheritedExpanded() {
  var toggles = $(".toggle-all", $("#doc-content"));
  toggles.each(function(i) {
    toggleAllInherited(this, true);
  });
  $("#toggleAllClassInherited").text("[Collapse All]");
}


/* HANDLE KEY EVENTS
 * - Listen for Ctrl+F (Cmd on Mac) and expand all inherited members (to aid page search)
 */
var agent = navigator['userAgent'].toLowerCase();
var mac = agent.indexOf("macintosh") != -1;

$(document).keydown( function(e) {
var control = mac ? e.metaKey && !e.ctrlKey : e.ctrlKey; // get ctrl key
  if (control && e.which == 70) {  // 70 is "F"
    ensureAllInheritedExpanded();
  }
});