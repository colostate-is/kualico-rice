/**
 * Copyright 2005-2013 The Kuali Foundation
 *
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.opensource.org/licenses/ecl2.php
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kuali.rice.krad.demo.uif.library.layoutmanagers;

import org.junit.Test;

import org.kuali.rice.testtools.selenium.WebDriverLegacyITBase;

/**
 * @author Kuali Rice Team (rice.collab@kuali.org)
 */
public class DemoLayoutManagersBoxLayoutAft extends WebDriverLegacyITBase {

    /**
     * /kr-krad/kradsampleapp?viewId=Demo-BoxLayoutManagerView&methodToCall=start
     */
    public static final String BOOKMARK_URL = "/kr-krad/kradsampleapp?viewId=Demo-BoxLayoutManagerView&methodToCall=start";

    @Override
    protected String getBookmarkUrl() {
        return BOOKMARK_URL;
    }

    @Override
    protected void navigate() throws Exception {
        waitAndClickById("Demo-LibraryLink", "");
        waitAndClickByLinkText("Layout Managers");
        waitAndClickByLinkText("Box Layout");
    }

    protected void testLayoutManagersVerticalBoxLayout() throws Exception {
       assertElementPresentByXpath("//div[@data-parent='Demo-BoxLayoutManager-Example1']/div/div/span/label");
       assertElementPresentByXpath("//div[@data-parent='Demo-BoxLayoutManager-Example1']/div/div/input[@name='inputField1']");
       assertElementPresentByXpath("//div[@data-parent='Demo-BoxLayoutManager-Example1']/div/div[2]/span/label");
       assertElementPresentByXpath("//div[@data-parent='Demo-BoxLayoutManager-Example1']/div/div[2]/input[@name='inputField2']");
    }
    
    protected void testLayoutManagersHorizontalBoxLayout() throws Exception {
        selectByName("exampleShown","Horizontal Box Layout");
        assertElementPresentByXpath("//div[@data-parent='Demo-BoxLayoutManager-Example2']/div/div[@class='uif-inputField uif-inputField-labelTop uif-boxLayoutHorizontalItem']/span/label");
        assertElementPresentByXpath("//div[@data-parent='Demo-BoxLayoutManager-Example2']/div/div[@class='uif-inputField uif-inputField-labelTop uif-boxLayoutHorizontalItem']/input[@name='inputField5']");
        assertElementPresentByXpath("//div[@data-parent='Demo-BoxLayoutManager-Example2']/div/div[@class='uif-inputField uif-inputField-labelTop uif-boxLayoutHorizontalItem'][2]/span/label");
        assertElementPresentByXpath("//div[@data-parent='Demo-BoxLayoutManager-Example2']/div/div[@class='uif-inputField uif-inputField-labelTop uif-boxLayoutHorizontalItem'][2]/input[@name='inputField6']");
    }
    
    protected void testLayoutManagersNestedExample1() throws Exception {
        selectByName("exampleShown","Nested Example 1");
        assertElementPresentByXpath("//div[@data-parent='Demo-BoxLayoutManager-Example3']/div[@class='uif-verticalBoxLayout clearfix']/div/div[@class='uif-horizontalBoxLayout clearfix']/div/span/label");
        assertElementPresentByXpath("//div[@data-parent='Demo-BoxLayoutManager-Example3']/div[@class='uif-verticalBoxLayout clearfix']/div/div[@class='uif-horizontalBoxLayout clearfix']/div/input[@name='inputField9']");
        assertElementPresentByXpath("//div[@data-parent='Demo-BoxLayoutManager-Example3']/div[@class='uif-verticalBoxLayout clearfix']/div/div[@class='uif-horizontalBoxLayout clearfix']/div[2]/span/label");
        assertElementPresentByXpath("//div[@data-parent='Demo-BoxLayoutManager-Example3']/div[@class='uif-verticalBoxLayout clearfix']/div/div[@class='uif-horizontalBoxLayout clearfix']/div[2]/input[@name='inputField10']");
    }
    
    protected void testLayoutManagersNestedExample2() throws Exception {
        selectByName("exampleShown","Nested Example 2");
        assertElementPresentByXpath("//div[@data-parent='Demo-BoxLayoutManager-Example4']/div[@class='uif-horizontalBoxLayout clearfix']/div/div[@class='uif-verticalBoxLayout clearfix']/div/span/label");
        assertElementPresentByXpath("//div[@data-parent='Demo-BoxLayoutManager-Example4']/div[@class='uif-horizontalBoxLayout clearfix']/div/div[@class='uif-verticalBoxLayout clearfix']/div/input[@name='inputField13']");
        assertElementPresentByXpath("//div[@data-parent='Demo-BoxLayoutManager-Example4']/div[@class='uif-horizontalBoxLayout clearfix']/div/div[@class='uif-verticalBoxLayout clearfix']/div[2]/span/label");
        assertElementPresentByXpath("//div[@data-parent='Demo-BoxLayoutManager-Example4']/div[@class='uif-horizontalBoxLayout clearfix']/div/div[@class='uif-verticalBoxLayout clearfix']/div[2]/input[@name='inputField14']");
    }
    
    protected void testLayoutManagersBoxLayoutPadding() throws Exception {
        selectByName("exampleShown","Padding");
        assertElementPresentByXpath("//div[@data-parent='Demo-BoxLayoutManager-Example5']/div/div[@style='padding-bottom: 50px;']/span/label");
        assertElementPresentByXpath("//div[@data-parent='Demo-BoxLayoutManager-Example5']/div/div[@style='padding-bottom: 50px;']/input[@name='inputField17']");
        assertElementPresentByXpath("//div[@data-parent='Demo-BoxLayoutManager-Example5']/div/div[@style='padding-bottom: 50px;'][2]/span/label");
        assertElementPresentByXpath("//div[@data-parent='Demo-BoxLayoutManager-Example5']/div/div[@style='padding-bottom: 50px;'][2]/input[@name='inputField18']");
    }
    
    protected void testLayoutManagersBoxLayoutItemCSS() throws Exception {
        selectByName("exampleShown","Item CSS");
        assertElementPresentByXpath("//div[@data-parent='Demo-BoxLayoutManager-Example6']/div/div/span[@class='uif-labelTop uif-labelBlock']");
        assertElementPresentByXpath("//div[@data-parent='Demo-BoxLayoutManager-Example6']/div/div/input[@name='inputField21']");
    }
    
    protected void testLayoutManagersBoxLayoutItemStyle() throws Exception {
        selectByName("exampleShown","Item style");
        assertElementPresentByXpath("//div[@data-parent='Demo-BoxLayoutManager-Example7']/div/div[@style='background-color: #99FF99; margin-right: 5px; padding: 5px;']/span");
        assertElementPresentByXpath("//div[@data-parent='Demo-BoxLayoutManager-Example7']/div/div[@style='background-color: #99FF99; margin-right: 5px; padding: 5px;']/input[@name='inputField24']");
    }
    
    @Test
    public void testLayoutManagersBoxLayoutBookmark() throws Exception {
        testLayoutManagersVerticalBoxLayout();
        testLayoutManagersHorizontalBoxLayout();
        testLayoutManagersNestedExample1();
        testLayoutManagersNestedExample2();
        testLayoutManagersBoxLayoutPadding();
        testLayoutManagersBoxLayoutItemCSS();
        testLayoutManagersBoxLayoutItemStyle();
        passed();
    }

    @Test
    public void testLayoutManagersBoxLayoutNav() throws Exception {
        testLayoutManagersVerticalBoxLayout();
        testLayoutManagersHorizontalBoxLayout();
        testLayoutManagersNestedExample1();
        testLayoutManagersNestedExample2();
        testLayoutManagersBoxLayoutPadding();
        testLayoutManagersBoxLayoutItemCSS();
        testLayoutManagersBoxLayoutItemStyle();
        passed();
    }  
}
