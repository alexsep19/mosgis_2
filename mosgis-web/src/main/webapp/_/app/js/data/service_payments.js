define ([], function () {

    $_DO.choose_tab_service_payments = function (e) {
        
        var layout = w2ui ['service_payments_layout']
            
        if (layout) {                
            layout.content ('main', '');
            layout.lock ('main', 'Загрузка...', true);
        }

        var name = e.tab.id

        localStorage.setItem ('service_payments.active_tab', name)

        use.block (name)

    }    

    return function (done) {

        var data = {}

        data.active_tab = localStorage.getItem ('service_payments.active_tab') || 'accounts'

        done (data)

    }

})