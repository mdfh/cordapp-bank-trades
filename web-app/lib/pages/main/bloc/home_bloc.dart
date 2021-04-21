import 'package:cordatradeclient/data/repository/trade_repo.dart';
import 'package:cordatradeclient/pages/dialog/bloc/dialog_bloc.dart';
import 'package:cordatradeclient/pages/dialog/bloc/dialog_event.dart';
import 'package:cordatradeclient/pages/main/bloc/home_event.dart';
import 'package:cordatradeclient/pages/main/bloc/home_state.dart';
import 'package:cordatradeclient/utils/party_ext.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class HomeBloc extends Bloc<HomeEvent, HomeState> {
  final TradeRepository repository;
  final DialogBloc dialogBloc;
  final String me;

  HomeBloc(this.me, this.repository, this.dialogBloc) : super(HomeLoadingState());

  @override
  Stream<HomeState> mapEventToState(HomeEvent event) async* {
    if (event is LoadHomeEvent)
      yield* _mapLoadHomeToState();
    else if (event is HomeEventSelectPeerEvent)
      yield* _mapLoadSelectPeerEventToState(event.selectedPeer);
    else if (event is HomeEventIssueTradeEvent)
      yield* _mapIssueTradeEventToState(event.amount);
  }

  Stream<HomeState> _mapLoadHomeToState() async* {
    try {
      final peers = await repository.getPeers();
      final trades = await repository.getTrades();
      yield HomeLoadedState(me, peers, trades);
    } catch (e) {}
  }

  Stream<HomeState> _mapLoadSelectPeerEventToState(String peer) async* {
    try {
      final loadedState = state as HomeLoadedState;
      yield loadedState.setSelectedPeer(peer);
    } catch (e) {}
  }

  Stream<HomeState> _mapIssueTradeEventToState(int amount) async* {
    try {
      final loadedState = state as HomeLoadedState;
      final response = await repository.issueTrade(
          amount, loadedState.selectedPeer?.getOrganization() ?? "");
      if (response) {
        dialogBloc.add(DialogSuccessEvent("Trade submitted successfully"));
        add(LoadHomeEvent());
      } else {
        dialogBloc.add(DialogSuccessEvent("Failed to submit the trade"));
      }
    } catch (e) {
      dialogBloc.add(DialogSuccessEvent("Failed to submit the trade"));
    }
  }
}
