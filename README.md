# Interactor

Interactor is a helper utility that helps generate clean code when following Bob Martins clean architecture pattern.

The library generates a file called "AbstractInteractorProvider" which implements "InteractorProvider".  This provider is used
by your Presenters as a single interface/ facade to the domain layer.  The domain layer will contain your interactors (or commands)
and services.  Because it implements an interface you can easily switch it out for unit testing :)

The generated code creates a factory method which instantiates the annotated class (which should have an empty constructor)
and calls execute passing as an argument the as subscriber of type - the super generic type of your annotated class.
 The idea being that you would create an interactor which subscribes to data of type T.  This is better explained with example code!

# How to use
1. Annotate your interactors or commands with @interactor
2. Extend AbstractInteractionProvider with your own implementation
3. Make your Provider accessible  to your Presenters via the Application or a ServiceLocator.

# Gradle
In your build script add
```
    repositories {
        maven { url 'https://dl.bintray.com/serenskye/mvp' }
    }
```

Then in dependancies add
```
 compile 'com.interactor:interactor-annotations:X.X.X'
 apt 'com.interactor:interactor-compiler:X.X.X'
```

# Example
Your interactor class :

```
@Interactor
public class DoSignIn extends RepositoryUseCase<AccountResponse, AccountsRepository> {

  public DoSignIn() {
    super(new AccountsRepositoryImpl());
  }
  @Override
  protected Observable<AccountResponse> buildUseCaseObservable() {
    return getRepository()
        .signIn(new AccountRequest("test@example.com", "12345678"))
        .doOnNext(account -> {
          DataServiceLocator.getSharedPrefWrapper().setAuthToken("account.getAuthToken());
        });
  }
}
```

The generated code
```
public abstract class AbstractInteractorProvider implements InteractorProvider {
  @Override
  public void executeDoSignIn(final Subscriber<AccountResponse> subscriber) {
    new DoSignIn().execute(subscriber);
  }
  }
```

Then from your presenter
```
getServices().executeDoSignIn(new SensibleSubscriber<AccountResponse>() {

      @Override
      public void onError(final Throwable e) {
        Timber.e(e, "DoSignIn onError: ");
      }

      @Override
      public void onNext(final AccountResponse accountResponse) {
        Timber.d("DoSignIn onNext: %s", accountResponse);
      }
    }));
```



