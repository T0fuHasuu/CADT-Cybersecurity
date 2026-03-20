package androidx.fragment.app;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.Transformation;
import androidx.core.os.CancellationSignal;
import androidx.core.view.OneShotPreDrawListener;
import androidx.fragment.R;
import androidx.fragment.app.FragmentTransition;

class FragmentAnim {
    private FragmentAnim() {
    }

    /* JADX WARNING: Missing exception handler attribute for start block: B:31:0x006f */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x006f A[SYNTHETIC, Splitter:B:31:0x006f] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    static androidx.fragment.app.FragmentAnim.AnimationOrAnimator loadAnimation(android.content.Context r4, androidx.fragment.app.Fragment r5, boolean r6, boolean r7) {
        /*
            int r0 = r5.getNextTransition()
            int r7 = getNextAnim(r5, r6, r7)
            r1 = 0
            r5.setAnimations(r1, r1, r1, r1)
            android.view.ViewGroup r1 = r5.mContainer
            r2 = 0
            if (r1 == 0) goto L_0x0022
            android.view.ViewGroup r1 = r5.mContainer
            int r3 = androidx.fragment.R.id.visible_removing_fragment_view_tag
            java.lang.Object r1 = r1.getTag(r3)
            if (r1 == 0) goto L_0x0022
            android.view.ViewGroup r1 = r5.mContainer
            int r3 = androidx.fragment.R.id.visible_removing_fragment_view_tag
            r1.setTag(r3, r2)
        L_0x0022:
            android.view.ViewGroup r1 = r5.mContainer
            if (r1 == 0) goto L_0x002f
            android.view.ViewGroup r1 = r5.mContainer
            android.animation.LayoutTransition r1 = r1.getLayoutTransition()
            if (r1 == 0) goto L_0x002f
            return r2
        L_0x002f:
            android.view.animation.Animation r1 = r5.onCreateAnimation(r0, r6, r7)
            if (r1 == 0) goto L_0x003b
            androidx.fragment.app.FragmentAnim$AnimationOrAnimator r4 = new androidx.fragment.app.FragmentAnim$AnimationOrAnimator
            r4.<init>((android.view.animation.Animation) r1)
            return r4
        L_0x003b:
            android.animation.Animator r5 = r5.onCreateAnimator(r0, r6, r7)
            if (r5 == 0) goto L_0x0047
            androidx.fragment.app.FragmentAnim$AnimationOrAnimator r4 = new androidx.fragment.app.FragmentAnim$AnimationOrAnimator
            r4.<init>((android.animation.Animator) r5)
            return r4
        L_0x0047:
            if (r7 != 0) goto L_0x004f
            if (r0 == 0) goto L_0x004f
            int r7 = transitToAnimResourceId(r0, r6)
        L_0x004f:
            if (r7 == 0) goto L_0x008b
            android.content.res.Resources r5 = r4.getResources()
            java.lang.String r5 = r5.getResourceTypeName(r7)
            java.lang.String r6 = "anim"
            boolean r5 = r6.equals(r5)
            if (r5 == 0) goto L_0x006f
            android.view.animation.Animation r6 = android.view.animation.AnimationUtils.loadAnimation(r4, r7)     // Catch:{ NotFoundException -> 0x006d, RuntimeException -> 0x006f }
            if (r6 == 0) goto L_0x008b
            androidx.fragment.app.FragmentAnim$AnimationOrAnimator r0 = new androidx.fragment.app.FragmentAnim$AnimationOrAnimator     // Catch:{ NotFoundException -> 0x006d, RuntimeException -> 0x006f }
            r0.<init>((android.view.animation.Animation) r6)     // Catch:{ NotFoundException -> 0x006d, RuntimeException -> 0x006f }
            return r0
        L_0x006d:
            r4 = move-exception
            throw r4
        L_0x006f:
            android.animation.Animator r6 = android.animation.AnimatorInflater.loadAnimator(r4, r7)     // Catch:{ RuntimeException -> 0x007b }
            if (r6 == 0) goto L_0x008b
            androidx.fragment.app.FragmentAnim$AnimationOrAnimator r0 = new androidx.fragment.app.FragmentAnim$AnimationOrAnimator     // Catch:{ RuntimeException -> 0x007b }
            r0.<init>((android.animation.Animator) r6)     // Catch:{ RuntimeException -> 0x007b }
            return r0
        L_0x007b:
            r6 = move-exception
            if (r5 != 0) goto L_0x008a
            android.view.animation.Animation r4 = android.view.animation.AnimationUtils.loadAnimation(r4, r7)
            if (r4 == 0) goto L_0x008b
            androidx.fragment.app.FragmentAnim$AnimationOrAnimator r5 = new androidx.fragment.app.FragmentAnim$AnimationOrAnimator
            r5.<init>((android.view.animation.Animation) r4)
            return r5
        L_0x008a:
            throw r6
        L_0x008b:
            return r2
        */
        throw new UnsupportedOperationException("Method not decompiled: androidx.fragment.app.FragmentAnim.loadAnimation(android.content.Context, androidx.fragment.app.Fragment, boolean, boolean):androidx.fragment.app.FragmentAnim$AnimationOrAnimator");
    }

    private static int getNextAnim(Fragment fragment, boolean z, boolean z2) {
        if (z2) {
            if (z) {
                return fragment.getPopEnterAnim();
            }
            return fragment.getPopExitAnim();
        } else if (z) {
            return fragment.getEnterAnim();
        } else {
            return fragment.getExitAnim();
        }
    }

    static void animateRemoveFragment(final Fragment fragment, AnimationOrAnimator animationOrAnimator, final FragmentTransition.Callback callback) {
        final View view = fragment.mView;
        final ViewGroup viewGroup = fragment.mContainer;
        viewGroup.startViewTransition(view);
        final CancellationSignal cancellationSignal = new CancellationSignal();
        cancellationSignal.setOnCancelListener(new CancellationSignal.OnCancelListener() {
            public void onCancel() {
                if (Fragment.this.getAnimatingAway() != null) {
                    View animatingAway = Fragment.this.getAnimatingAway();
                    Fragment.this.setAnimatingAway((View) null);
                    animatingAway.clearAnimation();
                }
                Fragment.this.setAnimator((Animator) null);
            }
        });
        callback.onStart(fragment, cancellationSignal);
        if (animationOrAnimator.animation != null) {
            EndViewTransitionAnimation endViewTransitionAnimation = new EndViewTransitionAnimation(animationOrAnimator.animation, viewGroup, view);
            fragment.setAnimatingAway(fragment.mView);
            endViewTransitionAnimation.setAnimationListener(new Animation.AnimationListener() {
                public void onAnimationRepeat(Animation animation) {
                }

                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    viewGroup.post(new Runnable() {
                        public void run() {
                            if (fragment.getAnimatingAway() != null) {
                                fragment.setAnimatingAway((View) null);
                                callback.onComplete(fragment, cancellationSignal);
                            }
                        }
                    });
                }
            });
            fragment.mView.startAnimation(endViewTransitionAnimation);
            return;
        }
        Animator animator = animationOrAnimator.animator;
        fragment.setAnimator(animationOrAnimator.animator);
        final Fragment fragment2 = fragment;
        final FragmentTransition.Callback callback2 = callback;
        animator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animator) {
                viewGroup.endViewTransition(view);
                Animator animator2 = fragment2.getAnimator();
                fragment2.setAnimator((Animator) null);
                if (animator2 != null && viewGroup.indexOfChild(view) < 0) {
                    callback2.onComplete(fragment2, cancellationSignal);
                }
            }
        });
        animator.setTarget(fragment.mView);
        animator.start();
    }

    private static int transitToAnimResourceId(int i, boolean z) {
        if (i == 4097) {
            return z ? R.animator.fragment_open_enter : R.animator.fragment_open_exit;
        }
        if (i != 4099) {
            if (i != 8194) {
                return -1;
            }
            return z ? R.animator.fragment_close_enter : R.animator.fragment_close_exit;
        } else if (z) {
            return R.animator.fragment_fade_enter;
        } else {
            return R.animator.fragment_fade_exit;
        }
    }

    static class AnimationOrAnimator {
        public final Animation animation;
        public final Animator animator;

        AnimationOrAnimator(Animation animation2) {
            this.animation = animation2;
            this.animator = null;
            if (animation2 == null) {
                throw new IllegalStateException("Animation cannot be null");
            }
        }

        AnimationOrAnimator(Animator animator2) {
            this.animation = null;
            this.animator = animator2;
            if (animator2 == null) {
                throw new IllegalStateException("Animator cannot be null");
            }
        }
    }

    static class EndViewTransitionAnimation extends AnimationSet implements Runnable {
        private boolean mAnimating = true;
        private final View mChild;
        private boolean mEnded;
        private final ViewGroup mParent;
        private boolean mTransitionEnded;

        EndViewTransitionAnimation(Animation animation, ViewGroup viewGroup, View view) {
            super(false);
            this.mParent = viewGroup;
            this.mChild = view;
            addAnimation(animation);
            viewGroup.post(this);
        }

        public boolean getTransformation(long j, Transformation transformation) {
            this.mAnimating = true;
            if (this.mEnded) {
                return !this.mTransitionEnded;
            }
            if (!super.getTransformation(j, transformation)) {
                this.mEnded = true;
                OneShotPreDrawListener.add(this.mParent, this);
            }
            return true;
        }

        public boolean getTransformation(long j, Transformation transformation, float f) {
            this.mAnimating = true;
            if (this.mEnded) {
                return !this.mTransitionEnded;
            }
            if (!super.getTransformation(j, transformation, f)) {
                this.mEnded = true;
                OneShotPreDrawListener.add(this.mParent, this);
            }
            return true;
        }

        public void run() {
            if (this.mEnded || !this.mAnimating) {
                this.mParent.endViewTransition(this.mChild);
                this.mTransitionEnded = true;
                return;
            }
            this.mAnimating = false;
            this.mParent.post(this);
        }
    }
}
