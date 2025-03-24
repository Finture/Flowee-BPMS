package com.finture.spin.groovy.xml.dom

if (child != null) {
    child = S(child)
}

element = S(input).append(child)
child.attr('id', 'child')
