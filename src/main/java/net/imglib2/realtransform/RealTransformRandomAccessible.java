/*
 * #%L
 * ImgLib2: a general-purpose, multidimensional image processing library.
 * %%
 * Copyright (C) 2009 - 2015 Tobias Pietzsch, Stephan Preibisch, Barry DeZonia,
 * Stephan Saalfeld, Curtis Rueden, Albert Cardona, Christian Dietz, Jean-Yves
 * Tinevez, Johannes Schindelin, Jonathan Hale, Lee Kamentsky, Larry Lindsey, Mark
 * Hiner, Michael Zinsmaier, Martin Horn, Grant Harris, Aivar Grislis, John
 * Bogovic, Steffen Jaensch, Stefan Helfrich, Jan Funke, Nick Perry, Mark Longair,
 * Melissa Linkert and Dimiter Prodanov.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package net.imglib2.realtransform;

import net.imglib2.Interval;
import net.imglib2.Point;
import net.imglib2.RandomAccess;
import net.imglib2.RandomAccessible;
import net.imglib2.RealInterval;
import net.imglib2.RealRandomAccess;
import net.imglib2.RealRandomAccessible;

/**
 * A {@link RandomAccessible} whose samples are generated by rasterizing a
 * {@link RealRandomAccessible} transformed by a {@link RealTransform}.
 * Changing the {@link RealTransform} will affect the
 * {@link RealTransformRandomAccessible} but not any existing
 * {@link RandomAccess} on it because each {@link RandomAccess} internally
 * works with a copy of the transform.  Make sure that you request a new
 * {@link RandomAccess} after modifying the transformation.
 * 
 * @author Stephan Saalfeld <saalfelds@janelia.hhmi.org>
 */
public class RealTransformRandomAccessible< T, R extends RealTransform > extends RealTransformRealRandomAccessible< T, R > implements RandomAccessible< T >
{
	/**
	 * {@link RealRandomAccess} that generates its samples from a source
	 * {@link RealRandomAccessible} at coordinates transformed by a
	 * {@link RealTransform}.
	 *
	 * This access does not move its sourceAccess while moving through space
	 * but executes {@link #apply()} on each {@link #get()} call.  This is
	 * preferable in situations where relative moves aren't more efficient than
	 * a full {@link #apply()} because moves execute only the integer part and
	 * not the coordinate transfer if no value is requested.
	 */
	public class RealTransformRandomAccess extends Point implements RandomAccess< T >
	{
		final protected RealRandomAccess< T > sourceAccess;

		final protected R transformCopy;

		@SuppressWarnings( "unchecked" )
		protected RealTransformRandomAccess()
		{
			super( transformToSource.numSourceDimensions() );
			sourceAccess = source.realRandomAccess();
			transformCopy = ( R )transformToSource.copy();
		}

		@SuppressWarnings( "unchecked" )
		protected RealTransformRandomAccess( final RealTransformRandomAccess a )
		{
			super( a );
			sourceAccess = a.sourceAccess.copyRealRandomAccess();
			transformCopy = ( R )a.transformCopy.copy();
		}

		final protected void apply()
		{
			transformCopy.apply( this, sourceAccess );
		}

		@Override
		public T get()
		{
			apply();
			return sourceAccess.get();
		}

		@Override
		public RealTransformRandomAccess copy()
		{
			return new RealTransformRandomAccess( this );
		}

		@Override
		public RealTransformRandomAccess copyRandomAccess()
		{
			return copy();
		}
	}

	public RealTransformRandomAccessible( final RealRandomAccessible< T > source, final R transformToSource )
	{
		super( source, transformToSource );
	}

	@Override
	public RealTransformRandomAccess randomAccess()
	{
		return new RealTransformRandomAccess();
	}

	/**
	 * To be overridden for {@link RealTransform} that can estimate the
	 * boundaries of a transferred {@link RealInterval}.
	 */
	@Override
	public RealTransformRandomAccess randomAccess( final Interval interval )
	{
		return randomAccess();
	}
}
