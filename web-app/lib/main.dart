import 'package:cordatradeclient/data/repository/trade_api_client.dart';
import 'package:cordatradeclient/trade_app.dart';
import 'package:cordatradeclient/trade_bloc_observer.dart';
import 'package:equatable/equatable.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

import 'data/repository/trade_repo.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  EquatableConfig.stringify = kDebugMode;
  Bloc.observer = TradeBlocObserver();
  runApp(TradeApp(
      tradeRepository:
          TradeRepository(tradeApiClient: TradeApiClient.instance)));
}
