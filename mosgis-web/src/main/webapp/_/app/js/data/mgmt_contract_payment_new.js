define ([], function () {

    $_DO.update_mgmt_contract_payment_new = function (e) {

        var form = w2ui ['contract_payment_new_form']

        var v = form.values ()
        
        var it = $('body').data ('data').item
        
        if (!v.begindate) die ('begindate', 'Укажите, пожалуйста, дату начала')

        if (!v.enddate) die ('enddate', 'Укажите, пожалуйста, дату окончания')
        if (v.enddate < v.begindate) die ('enddate', 'Дата начала превышает дату окончания управления')

        if (!(v.housemanagementpaymentsize) > 0) die ('housemanagementpaymentsize', 'Укажите, пожалуйста, корректный размер платы')
        
        v.uuid_contract = $_REQUEST.id

        query ({type: 'contract_payments', action: 'create', id: undefined}, {data: v}, function (data) {
        
            w2popup.close ()

            if (data.id) w2confirm ('Услуга зарегистрирована. Открыть её страницу в новой вкладке?').yes (function () {openTab ('/mgmt_contract_payment/' + data.id)})
            
            var grid = w2ui ['mgmt_contract_payments_grid']

            grid.reload (grid.refresh)
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))

        data.record = {
            uuid_contract_object: "",
            type_: data.item.code_vc_nsi_58 == 1 ? "P" : "C",
            begindate: dt_dmy (data.item.effectivedate),
            enddate:   dt_dmy (data.item.plandatecomptetion),
        }
                
        query ({type: "contract_objects", id: undefined}, {limit: 100000, offset: 0, search: [        
            {field: "uuid_contract", operator: "is", value: data.item.uuid},            
        ]}, function (d) {
        
            var a = []

            if (d.root.length == 1) {
                data.record.uuid_contract_object = d.root [0].id
            }
            else {
                a.push ({id: "", text: "Все объекты договора"})
            }
            
            $.each (d.root, function () {
                a.push ({id: this.id, text: this ['fias.label']})
            })
            
            data.objects = a
            
            done (data)

        })                      

    }

})