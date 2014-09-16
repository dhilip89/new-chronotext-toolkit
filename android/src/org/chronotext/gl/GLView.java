/*
 * THE NEW CHRONOTEXT TOOLKIT: https://github.com/arielm/new-chronotext-toolkit
 * COPYRIGHT (C) 2012-2014, ARIEL MALKA ALL RIGHTS RESERVED.
 *
 * THE FOLLOWING SOURCE-CODE IS DISTRIBUTED UNDER THE MODIFIED BSD LICENSE:
 * https://github.com/arielm/new-chronotext-toolkit/blob/master/LICENSE.md
 */

package org.chronotext.gl;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.egl.EGLDisplay;

import java.util.Vector;

import org.chronotext.gl.Touch;
import org.chronotext.Utils;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;

public class GLView extends GLSurfaceView
{
  protected GLRenderer mRenderer;

  protected boolean resumed;
  protected boolean attached;

  public GLView(Context context)
  {
    super(context);

    // setEGLContextClientVersion(2);
    // setEGLConfigChooser(8, 8, 8, 8, 0, 0);
    // getHolder().setFormat(PixelFormat.RGBA_8888);

    setEGLContextFactory(new CustomContextFactory(1)); // NECESSARY BECAUSE setPreserveEGLContextOnPause(true) CAN'T BE FULLY TRUSTED
    setPreserveEGLContextOnPause(true);
  }

  @Override
  public void setRenderer(Renderer renderer)
  {
    if (mRenderer == null)
    {
      super.setRenderer(renderer); // WILL START THE RENDERER'S THREAD
      mRenderer = (GLRenderer) renderer;

      queueEvent(new Runnable()
      {
        public void run()
        {
          mRenderer.performLaunch();
        }
      });
    }
  }

  @Override
  public void surfaceCreated(SurfaceHolder holder)
  {
    Utils.LOGD("GLView.surfaceCreated"); 
    super.surfaceCreated(holder);
  }

  @Override
  public void surfaceDestroyed(SurfaceHolder holder)
  {
    Utils.LOGD("GLView.surfaceDestroyed"); 
    super.surfaceDestroyed(holder);
  }

  @Override
  public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
  {
    Utils.LOGD("GLView.surfaceChanged: " + width + "x" + height);
    super.surfaceChanged(holder, format, width, height);
  }

  @Override
  public void onWindowFocusChanged(final boolean hasFocus)
  {
    Utils.LOGD("GLView.onWindowFocusChanged: " + hasFocus);
    super.onWindowFocusChanged(hasFocus);
  }

  @Override
  protected void onAttachedToWindow()
  {
    Utils.LOGD("GLView.onAttachedToWindow");

    super.onAttachedToWindow(); // WILL START A NEW RENDERER'S IF NECESSARY (E.G. WHEN THE GLView IS RE-ATTACHED)
    attached = true;

    queueEvent(new Runnable()
    {
      public void run()
      {
        mRenderer.onAttachedToWindow();
      }
    });
  }

  @Override
  protected void onDetachedFromWindow()
  {
    Utils.LOGD("GLView.onDetachedFromWindow");

    attached = false; // THE ONLY WAY TO COMMUNICATE WITH THE RENDERER'S THREAD BEFORE IT EXITS
    super.onDetachedFromWindow(); // WILL EXIT THE RENDERER'S THREAD (CustomContextFactory.destroyContext() WILL BE INVOKED BEFOREHAND ON THE RENDERER'S THREAD)
  }

  /*
   * RECEIVED ON THE MAIN-THREAD
   */
  @Override
  public void onResume()
  {
    Utils.LOGD("GLView.onResume");

    if (!resumed)
    {
      super.onResume();
      resumed = true;

      queueEvent(new Runnable()
      {
        public void run()
        {
          mRenderer.onResume();
        }
      });
    }
  }

  /*
   * RECEIVED ON THE MAIN-THREAD
   */
  @Override
  public void onPause()
  {
    Utils.LOGD("GLView.onPause");

    if (resumed)
    {
      super.onPause();
      resumed = false;

      queueEvent(new Runnable()
      {
        public void run()
        {
          mRenderer.onPause();
        }
      });
    }
  }

  @Override
  public void onVisibilityChanged(View changedView, final int visibility)
  {
    if (changedView == this)
    {
      queueEvent(new Runnable()
      {
        public void run()
        {
          mRenderer.onVisibilityChanged(visibility);
        }
      });
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent event)
  {
    switch (event.getAction() & MotionEvent.ACTION_MASK)
    {
      case MotionEvent.ACTION_DOWN :
      {
        int index = 0;
        final Vector<Touch> touches = new Vector<Touch>();
        touches.add(new Touch(event.getPointerId(index), event.getX(index), event.getY(index)));

        queueEvent(new Runnable()
        {
          public void run()
          {
            mRenderer.addTouches(touches);
          }
        });
        break;
      }

      case MotionEvent.ACTION_POINTER_DOWN :
      {
        int index = event.getActionIndex();
        final Vector<Touch> touches = new Vector<Touch>();
        touches.add(new Touch(event.getPointerId(index), event.getX(index), event.getY(index)));

        queueEvent(new Runnable()
        {
          public void run()
          {
            mRenderer.addTouches(touches);
          }
        });
        break;
      }

      case MotionEvent.ACTION_UP :
      {
        int index = 0;
        final Vector<Touch> touches = new Vector<Touch>();
        touches.add(new Touch(event.getPointerId(index), event.getX(index), event.getY(index)));

        queueEvent(new Runnable()
        {
          public void run()
          {
            mRenderer.removeTouches(touches);
          }
        });
        break;
      }

      case MotionEvent.ACTION_POINTER_UP :
      {
        int index = event.getActionIndex();
        final Vector<Touch> touches = new Vector<Touch>();
        touches.add(new Touch(event.getPointerId(index), event.getX(index), event.getY(index)));

        queueEvent(new Runnable()
        {
          public void run()
          {
            mRenderer.removeTouches(touches);
          }
        });
        break;
      }


      case MotionEvent.ACTION_MOVE :
      {
        final Vector<Touch> touches = new Vector<Touch>();
          
        for (int i = 0; i < event.getPointerCount(); i++)
        {
          touches.add(new Touch(event.getPointerId(i), event.getX(i), event.getY(i)));
        }

        queueEvent(new Runnable()
        {
          public void run()
          {
            mRenderer.updateTouches(touches);
          }
        });
        break;
      }
    }

    return true;
  }

  /*
   * BASED ON GLSurfaceView.DefaultContextFactory
   *
   * THE ONLY WAY TO BE NOTIFIED (ON THE RENDERER'S THREAD) WHEN:
   * - THE GL-CONTEXT HAS BEEN ACTUALLY CREATED OR DESTROYED
   * - THE GLView IS HAS BEEN DETACHED
   */
  protected class CustomContextFactory implements EGLContextFactory
  {
    protected int mEGLContextClientVersion;

    public CustomContextFactory(int eglContextClientVersion)
    {
      mEGLContextClientVersion = eglContextClientVersion;
    }

    public EGLContext createContext(EGL10 egl, EGLDisplay display, EGLConfig config)
    {
      Utils.LOGD("CustomContextFactory.createContext");

      mRenderer.contextCreated();

      int[] attrib_list = { 0x3098, mEGLContextClientVersion, EGL10.EGL_NONE };
      return egl.eglCreateContext(display, config, EGL10.EGL_NO_CONTEXT, mEGLContextClientVersion != 0 ? attrib_list : null);
    }

    public void destroyContext(EGL10 egl, EGLDisplay display, EGLContext context)
    {
      Utils.LOGD("CustomContextFactory.destroyContext");

      mRenderer.contextDestroyed(!attached);
      egl.eglDestroyContext(display, context);
    }
  }

  public void sendMessage(final int what, final String body)
  {
    queueEvent(new Runnable()
    {
      public void run()
      {
        mRenderer.sendMessage(what, body);
      }
    });
  }
}
