package org.esa.beam.decisiontree;

import java.awt.Color;

import org.esa.beam.framework.gpf.annotations.Parameter;

public class Classification {
	@Parameter
	private String name;
	@Parameter
	private int value;
	@Parameter
	private Color color;

    public Classification() {
    }
    
	public Classification(String name, String valueString, Color color) {
		this.name = name;
		value = Integer.parseInt(valueString);
		this.color = color;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getValue() {
		return value;
	}

	public void setValue(int value) {
		this.value = value;
	}
}
