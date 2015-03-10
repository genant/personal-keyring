/*
 * Copyright (C) 2014 Antonello Genuario
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package it.personal.keyring;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

public class LetterTileView extends View {
	private BitmapDrawable mDrawable;
	private LetterTileProvider mTileProvider;
	private int mWidth;
	private int mHeight;
	private String mDisplayName;
	private String mKey;
	public LetterTileView(Context context) {
		super(context);
		mTileProvider = new LetterTileProvider(context);
	}

	public LetterTileView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mTileProvider = new LetterTileProvider(context);
	}

	public LetterTileView(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		mTileProvider = new LetterTileProvider(context);
	}


	@Override
	protected void onDraw(Canvas canvas) {
//		if(mDrawable != null) {
//			mDrawable.draw(canvas);
//		}
		mTileProvider.drawLetterTile(canvas, mDisplayName, mKey, mWidth, mHeight);
	}
	
	public void setTileText(String displayName, String key) {
//	   final Bitmap letterTile = mTileProvider.getLetterTile(displayName, key, mWidth, mHeight);
//       mDrawable = new BitmapDrawable(getResources(), letterTile);
		mDisplayName = displayName;
		mKey = key;
	}
	public void setTileSize(int width, int height) {
		mWidth = width;
		mHeight = height;
	}
}
