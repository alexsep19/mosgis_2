define ([], function () {

    $_DO.update_charter_object_common_service_house_new = function (e) {

        var form = w2ui ['voc_user_form']

        var v = form.values ()

        if (!v.code_vc_nsi_3) die ('code_vc_nsi_3', 'Укажите, пожалуйста, вид услуги')
        
        var grid = w2ui ['charter_object_common_services_grid']
        
        v.uuid_charter_object = $_REQUEST.id

        query ({type: 'charter_object_services', action: 'create', id: undefined}, {data: v}, function () {
        
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
        
        query ({type: "charter_docs", id: undefined}, {search: [
        
            {field: "uuid_charter", operator: "is", value: data.item.uuid_charter},
            {field: "id_type",       operator: "is", value: 1},
            
        ]}, function (d) {
        
            var a = [{id: "", text: "Текущий устав"}]
            $.each (d.tb_charter_files, function () {
                a.push ({id: this.id, text: "Протокол собрания от " + dt_dmy (this.agreementdate) + " №" + this.agreementnumber})
            })            
            data.agreements = a
            

        })                      

    }

})