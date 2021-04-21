import 'package:cordatradeclient/pages/login/bloc/login_bloc.dart';
import 'package:cordatradeclient/pages/login/login_screen.dart';
import 'package:cordatradeclient/pages/main/bloc/home_bloc.dart';
import 'package:cordatradeclient/pages/main/bloc/home_event.dart';
import 'package:cordatradeclient/pages/main/home_screen.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'data/repository/trade_repo.dart';

class TradeApp extends StatelessWidget {
  const TradeApp({Key? key, required TradeRepository tradeRepository})
      : _tradeRepository = tradeRepository,
        super(key: key);

  final TradeRepository _tradeRepository;

  @override
  Widget build(BuildContext context) {
    return RepositoryProvider.value(
      value: _tradeRepository,
      child: BlocProvider(
        create: (_) => LoginBloc(_tradeRepository),
        child: TradeAppView(),
      ),
    );
  }
}

class TradeAppView extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      home: LoginScreen(),
    );
  }
}
