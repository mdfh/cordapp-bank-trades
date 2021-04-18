import 'package:cordatradeclient/data/repository/trade_repo.dart';
import 'package:cordatradeclient/pages/dialog/bloc/dialog_bloc.dart';
import 'package:cordatradeclient/pages/dialog/bloc/dialog_event.dart';
import 'package:cordatradeclient/pages/trade_detail/bloc/trade_detail_event.dart';
import 'package:cordatradeclient/pages/trade_detail/bloc/trade_detail_state.dart';
import 'package:flutter_bloc/flutter_bloc.dart';

class TradeDetailBloc extends Bloc<TradeDetailEvent, TradeDetailState> {
  final TradeRepository repository;
  final DialogBloc dialogBloc;
  final String me;

  TradeDetailBloc(this.repository, this.dialogBloc, this.me)
      : super(TradeDetailLoadingState());

  @override
  Stream<TradeDetailState> mapEventToState(TradeDetailEvent event) async* {
    if (event is LoadTradeDetailByIdEvent)
      yield* _mapLoadByIdToState(event.id);
    else if (event is LoadTradeDetailEvent)
      yield TradeDetailLoadedState(event.model);
    else if (event is TradeDetailMarkInProcessEvent)
      yield* _mapChangeStateToState(true);
    else if (event is TradeDetailMarkSettledEvent)
      yield* _mapChangeStateToState(false);
  }

  Stream<TradeDetailState> _mapChangeStateToState(bool isProcess) async* {
    final loadedState = state as TradeDetailLoadedState;
    try {
      if (me != loadedState.model.state.data.assignedTo) {
        dialogBloc.add(DialogSuccessEvent(
            "Only Assigned To party can update the status"));
        return;
      }
      yield loadedState.setLoading(true);
      final response = isProcess
          ? await repository
              .tradeInProcess(loadedState.model.state.data.linearId)
          : await repository
              .tradeSettled(loadedState.model.state.data.linearId);
      if (response) {
        dialogBloc.add(DialogSuccessEvent("State updated successfully"));
        add(LoadTradeDetailByIdEvent(loadedState.model.state.data.linearId));
      } else {
        yield loadedState.setLoading(false);
        dialogBloc.add(DialogErrorEvent("Failed to update state"));
      }
    } catch (e) {
      yield loadedState.setLoading(false);
      dialogBloc.add(DialogErrorEvent("Failed to update state"));
    }
  }

  Stream<TradeDetailState> _mapLoadByIdToState(String id) async* {
    try {
      final model = await repository.getTrades(id: id);
      if (model != null && model.isNotEmpty)
        yield TradeDetailLoadedState(model.first);
      else
        yield TradeDetailErrorState();
    } catch (e) {
      yield TradeDetailErrorState();
    }
  }
}
