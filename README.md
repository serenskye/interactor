# Interactor
This utility is designed to help developers who use a Bob Martins clean architecture pattern. More specifically the use of Interactors with
RXJava subscribers.  The utility uses annotation processing to generate a single facade (AbstractInteractorProvider) for executing your
interactors. The reasoning for this is so that your presentation layer has one interface into your domain, this keeps things simple and clean.
You can extend the AbstractInteractorProvider and add more services and domain layer actions.  For example, have a ServiceProvider which starts
location updates, gcm services and other background processes, as well as your usual network operations (via interactors).

The code generater also creates an interface InteractorProvider so that you may easily unit test your code :). Your annotated interactors
are expected to implement a baseclass (your choice) of Type T extends Subscriber.  This is so we can pass a subscriber as a parameter in the generated method.
Your annotated class may have any number of constructor methods and take 0-n arguments.

This is better explained with example code(below) and there is also an example project in github.

# How to use
1. Annotate your interactors or commands with @interactor
2. Extend AbstractInteractionProvider with your own implementation
3. Make your Provider accessible  to your Presenters via the Application or a ServiceLocator.

Note you will have to run build -> rebuild project in android studio to generate the files

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



