//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.4-2 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2014.05.17 at 01:09:47 PM CEST 
//


package generated;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;


/**
 * <p>Java class for positionType complex type.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.
 * 
 * <pre>
 * &lt;complexType name="positionType">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;sequence>
 *         &lt;element name="row" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *         &lt;element name="col" type="{http://www.w3.org/2001/XMLSchema}int"/>
 *       &lt;/sequence>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "positionType", propOrder = {
    "row",
    "col"
})
public class PositionType {

    protected int row;
    protected int col;

    /**
     * Gets the value of the row property.
     * 
     */
    public int getRow() {
        return row;
    }

    /**
     * Sets the value of the row property.
     * 
     */
    public void setRow(int value) {
        this.row = value;
    }

    /**
     * Gets the value of the col property.
     * 
     */
    public int getCol() {
        return col;
    }

    /**
     * Sets the value of the col property.
     * 
     */
    public void setCol(int value) {
        this.col = value;
    }

}
