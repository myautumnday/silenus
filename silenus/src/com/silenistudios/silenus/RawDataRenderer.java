package com.silenistudios.silenus;

import java.util.Vector;

import com.silenistudios.silenus.dom.BitmapInstance;
import com.silenistudios.silenus.dom.ShapeInstance;
import com.silenistudios.silenus.dom.Timeline;
import com.silenistudios.silenus.raw.AnimationBitmapData;
import com.silenistudios.silenus.raw.AnimationData;
import com.silenistudios.silenus.raw.AnimationShapeData;
import com.silenistudios.silenus.raw.ColorManipulation;
import com.silenistudios.silenus.raw.TransformationMatrix;

/**
 * This class will not render the animation to any video output, but will instead
 * "render" the resulting locations of each object at each frame to a data structure
 * that can be saved later for easy reconstruction of the animation.
 * This allows you to send the original .png images along with this data to completely
 * reproduce the animation without having to compute it real-time.
 * @author Karel
 *
 */
public class RawDataRenderer implements RenderInterface {
	
	// the scene
	Timeline fScene;
	
	// animation data
	AnimationData fData;
	
	// trandformation matrix stack
	Vector<TransformationMatrix> fTransformationStack = new Vector<TransformationMatrix>();
	
	// current transformation matrix
	TransformationMatrix fTransformationMatrix = new TransformationMatrix();
	
	// current color transformation
	ColorManipulation fColorManipulation = null;
	
	
	// render all data for a scene
	public RawDataRenderer(Timeline scene, int width, int height, int frameRate) {
		
		// create animation data
		fData = new AnimationData(scene.getAnimationLength(), width, height, frameRate);
		
		// create scene renderer
		SceneRenderer renderer = new SceneRenderer(scene, this);
		
		// go over all frames and render them
		for (int i = 0; i < scene.getAnimationLength(); ++i) {
			fData.setFrame(i);
			renderer.render(i);
		}
	}
	
	
	// get animation data
	public AnimationData getAnimationData() {
		return fData;
	}

	
	@Override
	public void save() {
		fTransformationStack.add(new TransformationMatrix(fTransformationMatrix.getMatrix(), fTransformationMatrix.getTranslateX(), fTransformationMatrix.getTranslateY()));
	}

	@Override
	public void restore() {
		fTransformationMatrix = fTransformationStack.lastElement();
		fTransformationStack.remove(fTransformationStack.size()-1);
	}

	@Override
	public void scale(double x, double y) {
		fTransformationMatrix = TransformationMatrix.compose(fTransformationMatrix, new TransformationMatrix(0.0, 0.0, x, y, 0.0));
	}

	@Override
	public void translate(double x, double y) {
		fTransformationMatrix = TransformationMatrix.compose(fTransformationMatrix, new TransformationMatrix(x, y, 1.0, 1.0, 0.0));
	}

	@Override
	public void rotate(double theta) {
		fTransformationMatrix = TransformationMatrix.compose(fTransformationMatrix, new TransformationMatrix(0.0, 0.0, 1.0, 1.0, theta));
	}

	@Override
	public void drawBitmapInstance(BitmapInstance img) {
		AnimationBitmapData bitmapData = new AnimationBitmapData(img, fTransformationMatrix, fColorManipulation);
		fData.addInstance(bitmapData);
		fColorManipulation = null;
	}
	
	@Override
	public void drawShapeInstance(ShapeInstance shape) {
		AnimationShapeData shapeData = new AnimationShapeData(shape, fTransformationMatrix);
		fData.addInstance(shapeData);
	}


	@Override
	public void applyColorManipulation(ColorManipulation colorManipulation) {
		fColorManipulation = colorManipulation;
	}


	@Override
	public void resetMask() {
		fData.resetMask();
	}
}