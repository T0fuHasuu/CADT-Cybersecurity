package kotlinx.coroutines.flow;

import kotlin.Metadata;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlin.coroutines.jvm.internal.DebugMetadata;
import kotlin.coroutines.jvm.internal.SuspendLambda;
import kotlin.jvm.functions.Function1;
import kotlin.jvm.functions.Function3;
import kotlinx.coroutines.CoroutineScope;

@Metadata(d1 = {"\u0000\u0012\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\u0010\u0000\u001a\u00020\u0001\"\u0004\b\u0000\u0010\u0002*\u00020\u00032\f\u0010\u0004\u001a\b\u0012\u0004\u0012\u0002H\u00020\u0005H@"}, d2 = {"<anonymous>", "", "T", "Lkotlinx/coroutines/CoroutineScope;", "downstream", "Lkotlinx/coroutines/flow/FlowCollector;"}, k = 3, mv = {1, 6, 0}, xi = 48)
@DebugMetadata(c = "kotlinx.coroutines.flow.FlowKt__DelayKt$debounceInternal$1", f = "Delay.kt", i = {0, 0, 0, 0, 1, 1, 1, 1}, l = {222, 355}, m = "invokeSuspend", n = {"downstream", "values", "lastValue", "timeoutMillis", "downstream", "values", "lastValue", "timeoutMillis"}, s = {"L$0", "L$1", "L$2", "L$3", "L$0", "L$1", "L$2", "L$3"})
/* compiled from: Delay.kt */
final class FlowKt__DelayKt$debounceInternal$1 extends SuspendLambda implements Function3<CoroutineScope, FlowCollector<? super T>, Continuation<? super Unit>, Object> {
    final /* synthetic */ Flow<T> $this_debounceInternal;
    final /* synthetic */ Function1<T, Long> $timeoutMillisSelector;
    private /* synthetic */ Object L$0;
    /* synthetic */ Object L$1;
    Object L$2;
    Object L$3;
    int label;

    /* JADX INFO: super call moved to the top of the method (can break code semantics) */
    FlowKt__DelayKt$debounceInternal$1(Function1<? super T, Long> function1, Flow<? extends T> flow, Continuation<? super FlowKt__DelayKt$debounceInternal$1> continuation) {
        super(3, continuation);
        this.$timeoutMillisSelector = function1;
        this.$this_debounceInternal = flow;
    }

    public final Object invoke(CoroutineScope coroutineScope, FlowCollector<? super T> flowCollector, Continuation<? super Unit> continuation) {
        FlowKt__DelayKt$debounceInternal$1 flowKt__DelayKt$debounceInternal$1 = new FlowKt__DelayKt$debounceInternal$1(this.$timeoutMillisSelector, this.$this_debounceInternal, continuation);
        flowKt__DelayKt$debounceInternal$1.L$0 = coroutineScope;
        flowKt__DelayKt$debounceInternal$1.L$1 = flowCollector;
        return flowKt__DelayKt$debounceInternal$1.invokeSuspend(Unit.INSTANCE);
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(15:7|26|27|30|(9:38|39|40|(1:42)|43|46|(1:48)|(1:50)(2:51|52)|50)(2:36|37)|32|38|39|40|(0)|43|46|(0)|(0)|50) */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0121, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0122, code lost:
        r13.handleBuilderException(r0);
     */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00e7  */
    /* JADX WARNING: Removed duplicated region for block: B:38:0x00ed  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x0106 A[Catch:{ all -> 0x0121 }] */
    /* JADX WARNING: Removed duplicated region for block: B:48:0x012f  */
    /* JADX WARNING: Removed duplicated region for block: B:50:0x0134 A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0135  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public final java.lang.Object invokeSuspend(java.lang.Object r18) {
        /*
            r17 = this;
            r1 = r17
            java.lang.Object r0 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
            int r2 = r1.label
            r3 = 2
            r4 = 1
            r5 = 0
            r7 = 0
            if (r2 == 0) goto L_0x004b
            if (r2 == r4) goto L_0x0034
            if (r2 != r3) goto L_0x002c
            java.lang.Object r2 = r1.L$3
            kotlin.jvm.internal.Ref$LongRef r2 = (kotlin.jvm.internal.Ref.LongRef) r2
            java.lang.Object r2 = r1.L$2
            kotlin.jvm.internal.Ref$ObjectRef r2 = (kotlin.jvm.internal.Ref.ObjectRef) r2
            java.lang.Object r8 = r1.L$1
            kotlinx.coroutines.channels.ReceiveChannel r8 = (kotlinx.coroutines.channels.ReceiveChannel) r8
            java.lang.Object r9 = r1.L$0
            kotlinx.coroutines.flow.FlowCollector r9 = (kotlinx.coroutines.flow.FlowCollector) r9
            kotlin.ResultKt.throwOnFailure(r18)
            r10 = r9
            r9 = r8
            r8 = r2
            r2 = r1
            goto L_0x0139
        L_0x002c:
            java.lang.IllegalStateException r0 = new java.lang.IllegalStateException
            java.lang.String r2 = "call to 'resume' before 'invoke' with coroutine"
            r0.<init>(r2)
            throw r0
        L_0x0034:
            java.lang.Object r2 = r1.L$3
            kotlin.jvm.internal.Ref$LongRef r2 = (kotlin.jvm.internal.Ref.LongRef) r2
            java.lang.Object r8 = r1.L$2
            kotlin.jvm.internal.Ref$ObjectRef r8 = (kotlin.jvm.internal.Ref.ObjectRef) r8
            java.lang.Object r9 = r1.L$1
            kotlinx.coroutines.channels.ReceiveChannel r9 = (kotlinx.coroutines.channels.ReceiveChannel) r9
            java.lang.Object r10 = r1.L$0
            kotlinx.coroutines.flow.FlowCollector r10 = (kotlinx.coroutines.flow.FlowCollector) r10
            kotlin.ResultKt.throwOnFailure(r18)
            r11 = r2
            r2 = r1
            goto L_0x00bf
        L_0x004b:
            kotlin.ResultKt.throwOnFailure(r18)
            java.lang.Object r2 = r1.L$0
            r8 = r2
            kotlinx.coroutines.CoroutineScope r8 = (kotlinx.coroutines.CoroutineScope) r8
            java.lang.Object r2 = r1.L$1
            kotlinx.coroutines.flow.FlowCollector r2 = (kotlinx.coroutines.flow.FlowCollector) r2
            r9 = 0
            r10 = 0
            kotlinx.coroutines.flow.FlowKt__DelayKt$debounceInternal$1$values$1 r11 = new kotlinx.coroutines.flow.FlowKt__DelayKt$debounceInternal$1$values$1
            kotlinx.coroutines.flow.Flow<T> r12 = r1.$this_debounceInternal
            r11.<init>(r12, r7)
            kotlin.jvm.functions.Function2 r11 = (kotlin.jvm.functions.Function2) r11
            r12 = 3
            r13 = 0
            kotlinx.coroutines.channels.ReceiveChannel r8 = kotlinx.coroutines.channels.ProduceKt.produce$default(r8, r9, r10, r11, r12, r13)
            kotlin.jvm.internal.Ref$ObjectRef r9 = new kotlin.jvm.internal.Ref$ObjectRef
            r9.<init>()
            r10 = r2
            r2 = r1
            r16 = r9
            r9 = r8
            r8 = r16
        L_0x0074:
            T r11 = r8.element
            kotlinx.coroutines.internal.Symbol r12 = kotlinx.coroutines.flow.internal.NullSurrogateKt.DONE
            if (r11 == r12) goto L_0x013d
            kotlin.jvm.internal.Ref$LongRef r11 = new kotlin.jvm.internal.Ref$LongRef
            r11.<init>()
            T r12 = r8.element
            if (r12 == 0) goto L_0x00c1
            kotlin.jvm.functions.Function1<T, java.lang.Long> r12 = r2.$timeoutMillisSelector
            kotlinx.coroutines.internal.Symbol r13 = kotlinx.coroutines.flow.internal.NullSurrogateKt.NULL
            T r14 = r8.element
            if (r14 != r13) goto L_0x008c
            r14 = r7
        L_0x008c:
            java.lang.Object r12 = r12.invoke(r14)
            java.lang.Number r12 = (java.lang.Number) r12
            long r12 = r12.longValue()
            r11.element = r12
            long r12 = r11.element
            int r12 = (r12 > r5 ? 1 : (r12 == r5 ? 0 : -1))
            if (r12 < 0) goto L_0x00ca
            long r12 = r11.element
            int r12 = (r12 > r5 ? 1 : (r12 == r5 ? 0 : -1))
            if (r12 != 0) goto L_0x00c1
            kotlinx.coroutines.internal.Symbol r12 = kotlinx.coroutines.flow.internal.NullSurrogateKt.NULL
            T r13 = r8.element
            if (r13 != r12) goto L_0x00ab
            r13 = r7
        L_0x00ab:
            r12 = r2
            kotlin.coroutines.Continuation r12 = (kotlin.coroutines.Continuation) r12
            r2.L$0 = r10
            r2.L$1 = r9
            r2.L$2 = r8
            r2.L$3 = r11
            r2.label = r4
            java.lang.Object r12 = r10.emit(r13, r12)
            if (r12 != r0) goto L_0x00bf
            return r0
        L_0x00bf:
            r8.element = r7
        L_0x00c1:
            r16 = r2
            r2 = r0
            r0 = r11
            r11 = r10
            r10 = r9
            r9 = r16
            goto L_0x00d6
        L_0x00ca:
            java.lang.IllegalArgumentException r0 = new java.lang.IllegalArgumentException
            java.lang.String r2 = "Debounce timeout should not be negative"
            java.lang.String r2 = r2.toString()
            r0.<init>(r2)
            throw r0
        L_0x00d6:
            boolean r12 = kotlinx.coroutines.DebugKt.getASSERTIONS_ENABLED()
            if (r12 == 0) goto L_0x00ed
            T r12 = r8.element
            if (r12 == 0) goto L_0x00ed
            long r12 = r0.element
            int r12 = (r12 > r5 ? 1 : (r12 == r5 ? 0 : -1))
            if (r12 <= 0) goto L_0x00e7
            goto L_0x00ed
        L_0x00e7:
            java.lang.AssertionError r0 = new java.lang.AssertionError
            r0.<init>()
            throw r0
        L_0x00ed:
            r9.L$0 = r11
            r9.L$1 = r10
            r9.L$2 = r8
            r9.L$3 = r0
            r9.label = r3
            r12 = r9
            kotlin.coroutines.Continuation r12 = (kotlin.coroutines.Continuation) r12
            kotlinx.coroutines.selects.SelectBuilderImpl r13 = new kotlinx.coroutines.selects.SelectBuilderImpl
            r13.<init>(r12)
            r14 = r13
            kotlinx.coroutines.selects.SelectBuilder r14 = (kotlinx.coroutines.selects.SelectBuilder) r14     // Catch:{ all -> 0x0121 }
            T r15 = r8.element     // Catch:{ all -> 0x0121 }
            if (r15 == 0) goto L_0x0112
            long r3 = r0.element     // Catch:{ all -> 0x0121 }
            kotlinx.coroutines.flow.FlowKt__DelayKt$debounceInternal$1$3$1 r0 = new kotlinx.coroutines.flow.FlowKt__DelayKt$debounceInternal$1$3$1     // Catch:{ all -> 0x0121 }
            r0.<init>(r11, r8, r7)     // Catch:{ all -> 0x0121 }
            kotlin.jvm.functions.Function1 r0 = (kotlin.jvm.functions.Function1) r0     // Catch:{ all -> 0x0121 }
            r14.onTimeout(r3, r0)     // Catch:{ all -> 0x0121 }
        L_0x0112:
            kotlinx.coroutines.selects.SelectClause1 r0 = r10.getOnReceiveCatching()     // Catch:{ all -> 0x0121 }
            kotlinx.coroutines.flow.FlowKt__DelayKt$debounceInternal$1$3$2 r3 = new kotlinx.coroutines.flow.FlowKt__DelayKt$debounceInternal$1$3$2     // Catch:{ all -> 0x0121 }
            r3.<init>(r8, r11, r7)     // Catch:{ all -> 0x0121 }
            kotlin.jvm.functions.Function2 r3 = (kotlin.jvm.functions.Function2) r3     // Catch:{ all -> 0x0121 }
            r14.invoke(r0, r3)     // Catch:{ all -> 0x0121 }
            goto L_0x0125
        L_0x0121:
            r0 = move-exception
            r13.handleBuilderException(r0)
        L_0x0125:
            java.lang.Object r0 = r13.getResult()
            java.lang.Object r3 = kotlin.coroutines.intrinsics.IntrinsicsKt.getCOROUTINE_SUSPENDED()
            if (r0 != r3) goto L_0x0132
            kotlin.coroutines.jvm.internal.DebugProbesKt.probeCoroutineSuspended(r12)
        L_0x0132:
            if (r0 != r2) goto L_0x0135
            return r2
        L_0x0135:
            r0 = r2
            r2 = r9
            r9 = r10
            r10 = r11
        L_0x0139:
            r3 = 2
            r4 = 1
            goto L_0x0074
        L_0x013d:
            kotlin.Unit r0 = kotlin.Unit.INSTANCE
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: kotlinx.coroutines.flow.FlowKt__DelayKt$debounceInternal$1.invokeSuspend(java.lang.Object):java.lang.Object");
    }
}
