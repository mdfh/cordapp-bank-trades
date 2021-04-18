import 'package:cordatradeclient/data/repository/trade_repo.dart';
import 'package:cordatradeclient/pages/login/bloc/login_event.dart';
import 'package:cordatradeclient/pages/login/bloc/login_state.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class LoginBloc extends Bloc<LoginEvent, LoginState> {
  late TradeRepository repository;

  LoginBloc(this.repository) : super(LoginInitState());

  @override
  Stream<LoginState> mapEventToState(LoginEvent event) async* {
    if (event is LoginInitEvent) yield* _mapLoginToState(event);
  }

  Stream<LoginState> _mapLoginToState(LoginInitEvent event) async* {
    try {
      yield LoginLoadingState();
      final result = await repository.getMyInformation(event.domain, event.port, event.username, event.password);
      if(result != null)
        {
          yield LoginSuccessState(result);
        }
      else
        {
          yield LoginErrorState();
        }
    } catch (e) {
      yield LoginErrorState();
    }
  }
}
