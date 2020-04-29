package pers.jay.wanandroid.mvp.ui.activity;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.widget.FrameLayout;

import com.jess.arms.base.BaseActivity;
import com.jess.arms.di.component.AppComponent;
import com.jess.arms.utils.ArmsUtils;

import org.simple.eventbus.Subscriber;

import butterknife.BindView;
import butterknife.ButterKnife;
import pers.jay.wanandroid.R;
import pers.jay.wanandroid.common.Const;
import pers.jay.wanandroid.common.JApplication;
import pers.jay.wanandroid.di.component.DaggerMainComponent;
import pers.jay.wanandroid.event.Event;
import pers.jay.wanandroid.mvp.contract.MainContract;
import pers.jay.wanandroid.mvp.presenter.MainPresenter;
import pers.jay.wanandroid.mvp.ui.fragment.ContainerFragment;
import pers.jay.wanandroid.mvp.ui.fragment.LoginFragment;
import pers.jay.wanandroid.mvp.ui.fragment.SplashFragment;
import pers.zjc.commonlibs.util.FragmentUtils;
import timber.log.Timber;

import static com.jess.arms.utils.Preconditions.checkNotNull;

public class MainActivity extends BaseActivity<MainPresenter> implements MainContract.View {

    @BindView(R.id.flContainer)
    FrameLayout flContainer;
    /* 当前fragment */
    private Fragment curFragment;

    private FragmentManager mFragmentManager;

    @Override
    public void setupActivityComponent(@NonNull AppComponent appComponent) {
        DaggerMainComponent //如找不到该类,请编译一下项目
                            .builder().appComponent(appComponent).view(this).build().inject(this);
    }

    @Override
    public int initView(@Nullable Bundle savedInstanceState) {
        return R.layout.activity_main; //如果你不需要框架帮你设置 setContentView(id) 需要自行设置,请返回 0
    }

    @Override
    public void initData(@Nullable Bundle savedInstanceState) {
//        switchFragment(SplashFragment.instantiate(this, SplashFragment.class.getName()));
//        FragmentUtils.add(getSupportFragmentManager(), SplashFragment.newInstance(), R.id.flContainer, false, false);
        FragmentUtils.add(getSupportFragmentManager(), ContainerFragment.newInstance(), R.id.flContainer, false, true);
        // 灰度化方案二(清明节灰化app)，来源：鸿洋公众号：https://mp.weixin.qq.com/s/EioJ8ogsCxQEFm44mKFiOQ
//        Paint paint = new Paint();
//        ColorMatrix cm = new ColorMatrix();
//        cm.setSaturation(0);
//        paint.setColorFilter(new ColorMatrixColorFilter(cm));
//        getWindow().getDecorView().setLayerType(View.LAYER_TYPE_HARDWARE, paint);
    }

    private void setFullScreen(boolean isFullScreen) {
        setTheme(isFullScreen ? R.style.LaunchTheme : R.style.AppTheme);
    }

    @Override
    public void showMessage(@NonNull String message) {
        checkNotNull(message);
        ArmsUtils.snackbarText(message);
    }

    @Override
    public void launchActivity(@NonNull Intent intent) {
        checkNotNull(intent);
        ArmsUtils.startActivity(intent);
    }

    @Override
    public void killMyself() {
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppTheme);
        super.onCreate(savedInstanceState);
        ButterKnife.bind(this);
        mFragmentManager = getSupportFragmentManager();
    }

    /**
     * 统一切换fragment的方法,不实例化多个Fragment，避免卡顿,Fragment中执行状态切换后的回调onHiddenChanged(boolean hidden)
     * @param targetFragment 跳转目标fragment
     */
    public void switchFragment(Fragment targetFragment, Bundle args) {
        if (mFragmentManager != null) {
        FragmentTransaction trans = mFragmentManager.beginTransaction();
            if (!targetFragment.isAdded()) {
                // 首次执行curFragment为空，需要判断
                if (curFragment != null) {
                    trans.hide(curFragment);
                }
                trans.add(R.id.flContainer, targetFragment, targetFragment.getClass().getSimpleName());
                trans.addToBackStack(targetFragment.getClass().getSimpleName());
                trans.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);    //设置动画效果
            } else {
//                targetFragment = mFragmentManager.findFragmentByTag(targetFragment.getClass().getSimpleName());
                trans.hide(curFragment).show(targetFragment);
            }
            trans.commitAllowingStateLoss();
            if (curFragment instanceof SplashFragment) {
                trans.remove(curFragment);
            }
            curFragment = targetFragment;
        }
    }

    public void switchFragment(Fragment targetFragment) {
        switchFragment(targetFragment, null);
    }

    public void addFrag(Fragment targetFragment) {
        FragmentUtils.add(getSupportFragmentManager(), targetFragment, R.id.flContainer);
    }

    /**
     *
     * @param fragment
     */
    public void replaceFragment(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        if (fm != null) {
            FragmentTransaction trans = fm.beginTransaction();
            trans.replace(R.id.flContainer, fragment);
            trans.commitAllowingStateLoss();
        }
    }

    public void showHideFragment(Fragment sourceFragment, Fragment destFragment) {
        FragmentUtils.showHide(destFragment, sourceFragment);
    }

    public void switchToLogin() {
        FragmentUtils.add(mFragmentManager, LoginFragment.newInstance(), R.id.flContainer);
    }

    @Override
    public boolean useFragment() {
        return true;
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Activity 意外销毁时保存状态
        outState.putBoolean(Const.Key.SAVE_INSTANCE_STATE, true);
        super.onSaveInstanceState(outState);
    }

    @Subscriber
    public void onUiModeChanged(Event event) {
        if (event.getEventCode() == Const.EventCode.CHANGE_UI_MODE) {
            Timber.e("MainActivity准备重建");
            JApplication.avoidSplashRecreate(MainActivity.this, MainActivity.class);
        }
    }
}
