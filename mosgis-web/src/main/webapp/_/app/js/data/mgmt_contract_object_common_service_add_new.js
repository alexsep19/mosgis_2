define ([], function () {

    $_DO.update_mgmt_contract_object_common_service_add_new = function (e) {

        var form = w2ui ['voc_user_form']

        var v = form.values ()

        if (!v.uuid_add_service) die ('uuid_add_service', 'Укажите, пожалуйста, вид услуги')
        
        var grid = w2ui ['mgmt_contract_object_common_services_grid']
        
        v.uuid_contract_object = $_REQUEST.id

        query ({type: 'contract_object_services', action: 'create', id: undefined}, {data: v}, function () {
        
            w2popup.close ()
            
            grid.reload (grid.refresh)
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))

        data.record = {
            uuid_contract_agreement: "",
            startdate: dt_dmy (data.item.startdate),
            enddate:   dt_dmy (data.item.enddate),
        }
        
        query ({type: "contract_docs", id: undefined}, {search: [
        
            {field: "uuid_contract", operator: "is", value: data.item.uuid_contract},
            {field: "id_type",       operator: "is", value: 1},
            
        ]}, function (d) {
        
            var a = [{id: "", text: "Текущий договор управления"}]
            $.each (d.tb_contract_files, function () {
                a.push ({id: this.id, text: "Доп. соглашение от " + dt_dmy (this.agreementdate) + " №" + this.agreementnumber})
            })            
            data.agreements = a
            
            done (data)

        })                      

    }

})