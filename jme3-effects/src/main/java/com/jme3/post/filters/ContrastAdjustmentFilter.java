/*
 * Copyright (c) 2009-2021 jMonkeyEngine
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * * Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * * Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * * Neither the name of 'jMonkeyEngine' nor the names of its contributors
 *   may be used to endorse or promote products derived from this software
 *   without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.jme3.post.filters;

import com.jme3.asset.AssetManager;
import com.jme3.export.InputCapsule;
import com.jme3.export.JmeExporter;
import com.jme3.export.JmeImporter;
import com.jme3.export.OutputCapsule;
import com.jme3.material.Material;
import com.jme3.post.Filter;
import com.jme3.renderer.RenderManager;
import com.jme3.renderer.ViewPort;
import java.io.IOException;

/**
 * A filter to adjust the colors of a rendered scene by normalizing each color channel to a specified range,
 * applying a power law, and scaling the output. The alpha channel is unaffected.
 *
 * @author pavl_g.
 */
public class ContrastAdjustmentFilter extends Filter {

    protected float redChannelExponent;
    protected float greenChannelExponent;
    protected float blueChannelExponent;
    /**
     * Lower limit of the input range for all color channels: a level that the filter normalizes to 0.
     */
    protected float lowerLimit;
    /**
     * Upper limit of the input range for all color channels: the level that the filter normalizes to 1
     * (before output scaling).
     */
    protected float upperLimit;
    //the final pass scale factor
    protected float redChannelScale;
    protected float greenChannelScale;
    protected float blueChannelScale;
    protected Material material;

    /**
     * Instantiates a default color contrast filter, default input range and default scale.
     * Default values :
     * - Exponents = 1.0f on all channels.
     * - Input Range Lower Limit = 0f.
     * - Input Range Upper Limit = 1f.
     * - Scale = 1.0f on all channels.
     */
    public ContrastAdjustmentFilter() {
        this(1f);
    }

    /**
     * Instantiates a color contrast filter with a specific exponent, default scale and default input range.
     *
     * @param exponent an exponent to apply on all channels.
     */
    public ContrastAdjustmentFilter(float exponent) {
        setExponents(exponent, exponent, exponent);
    }


    /**
     * Sets the exponents used to adjust the contrast of the color channels.
     * Default values are 1f.
     *
     * @param redChannelExponent   the red channel exponent.
     * @param greenChannelExponent the green channel exponent.
     * @param blueChannelExponent  the blue channel exponent.
     * @return this filter instance for a chain call.
     */
    public ContrastAdjustmentFilter setExponents(float redChannelExponent, float greenChannelExponent, float blueChannelExponent) {
        this.redChannelExponent = redChannelExponent;
        this.greenChannelExponent = greenChannelExponent;
        this.blueChannelExponent = blueChannelExponent;

        if (material == null) {
            return this;
        }
        //different channels exp for different transfer functions
        material.setFloat("redChannelExponent", redChannelExponent);
        material.setFloat("greenChannelExponent", greenChannelExponent);
        material.setFloat("blueChannelExponent", blueChannelExponent);
        return this;
    }

    /**
     * Retrieves the red channel exponent.
     * Default value = 1.0f
     *
     * @return the red channel exponent.
     */
    public float getRedChannelExponent() {
        return redChannelExponent;
    }

    /**
     * Retrieves the green channel exponent.
     * Default value = 1.0f.
     *
     * @return the green channel exponent.
     */
    public float getGreenChannelExponent() {
        return greenChannelExponent;
    }

    /**
     * Retrieves the blue channel exponent.
     * Default value = 1.0f
     *
     * @return the blue channel exponent.
     */
    public float getBlueChannelExponent() {
        return blueChannelExponent;
    }

    /**
     * Sets the input range for all color channels. Before applying the power law, the input levels get
     * normalized so that lowerLimit becomes 0 and upperLimit becomes 1.
     *
     * @param lowerLimit the desired lower limit (default=0)
     * @param upperLimit the desired upper limit (default=1)
     * @return this filter instance for a chain call.
     */
    public ContrastAdjustmentFilter setInputRange(float lowerLimit, float upperLimit) {
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;

        if (material == null) {
            return this;
        }

        //inputRange values
        material.setFloat("lowerLimit", lowerLimit);
        material.setFloat("upperLimit", upperLimit);
        return this;
    }

    /**
     * Returns the lower limit of the input range.
     * Default value = 0.0.
     *
     * @return the lower limit
     */
    public float getInputRangeLowerLimit() {
        return lowerLimit;
    }

    /**
     * Returns the upper limit of the input range.
     * Default value = 1.0.
     *
     * @return the upper limit
     */
    public float getInputRangeUpperLimit() {
        return upperLimit;
    }

    /**
     * Adjusts the scales of different channels.
     * Default values = 1.0.
     *
     * @param redChannelScale   the red channel scale.
     * @param greenChannelScale the green channel scale.
     * @param blueChannelScale  the blue channel scale.
     * @return this filter instance for a chain call.
     */
    public ContrastAdjustmentFilter setScales(float redChannelScale, float greenChannelScale, float blueChannelScale) {
        this.redChannelScale = redChannelScale;
        this.greenChannelScale = greenChannelScale;
        this.blueChannelScale = blueChannelScale;

        if (material == null) {
            return this;
        }

        //adjust the scales of different channels through the material file
        material.setFloat("redChannelScale", redChannelScale);
        material.setFloat("greenChannelScale", greenChannelScale);
        material.setFloat("blueChannelScale", blueChannelScale);
        return this;
    }

    /**
     * Retrieves the value of the red channel scale that's applied on the final pass.
     * Default value = 1.0.
     *
     * @return the scale of the red channel.
     */
    public float getRedChannelScale() {
        return redChannelScale;
    }

    /**
     * Retrieves the value of the green channel scale that's applied on the final pass.
     * Default value = 1.0.
     *
     * @return the scale of the green channel.
     */
    public float getGreenChannelScale() {
        return greenChannelScale;
    }

    /**
     * Retrieves the value of the blue channel scale that's applied on the final pass.
     * Default value = 1.0.
     *
     * @return the scale of the blue channel.
     */
    public float getBlueChannelScale() {
        return blueChannelScale;
    }

    @Override
    protected void initFilter(AssetManager manager, RenderManager renderManager, ViewPort vp, int w, int h) {
        //validate app
        if (manager == null || renderManager == null || vp == null || w == 0 || h == 0) {
            return;
        }
        material = new Material(manager, "Common/MatDefs/Post/ColorContrast.j3md");

        //different channels exp for different transfer functions
        setExponents(redChannelExponent, greenChannelExponent, blueChannelExponent);

        //input range
        setInputRange(lowerLimit, upperLimit);

        //final pass scales
        setScales(redChannelScale, greenChannelScale, blueChannelScale);
    }

    @Override
    protected Material getMaterial() {
        if (material == null) {
            throw new IllegalStateException("Cannot create a color filter from a null reference !");
        }
        return material;
    }

    @Override
    public void read(JmeImporter im) throws IOException {
        super.read(im);
        final InputCapsule inputCapsule = im.getCapsule(this);
        redChannelExponent = inputCapsule.readFloat("redChannelExponent", 1f);
        greenChannelExponent = inputCapsule.readFloat("greenChannelExponent", 1f);
        blueChannelExponent = inputCapsule.readFloat("blueChannelExponent", 1f);
        lowerLimit = inputCapsule.readFloat("lowerLimit", 0f);
        upperLimit = inputCapsule.readFloat("upperLimit", 1f);
        redChannelScale = inputCapsule.readFloat("redChannelScale", 1f);
        greenChannelScale = inputCapsule.readFloat("greenChannelScale", 1f);
        blueChannelScale = inputCapsule.readFloat("blueChannelScale", 1f);
    }

    @Override
    public void write(JmeExporter ex) throws IOException {
        super.write(ex);
        final OutputCapsule outputCapsule = ex.getCapsule(this);
        outputCapsule.write(redChannelExponent, "redChannelExponent", 1f);
        outputCapsule.write(greenChannelExponent, "greenChannelExponent", 1f);
        outputCapsule.write(blueChannelExponent, "blueChannelExponent", 1f);
        outputCapsule.write(lowerLimit, "lowerLimit", 0f);
        outputCapsule.write(upperLimit, "upperLimit", 1f);
        outputCapsule.write(redChannelScale, "redChannelScale", 1f);
        outputCapsule.write(greenChannelScale, "greenChannelScale", 1f);
        outputCapsule.write(blueChannelScale, "blueChannelScale", 1f);
    }
}