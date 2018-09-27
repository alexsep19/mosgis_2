define ([], function () {

    $_DO.update_mgmt_contract_object_new = function (e) {

        var form = w2ui ['voc_user_form']

        var v = form.values ()

        if (!v.fiashouseguid) die ('fiashouseguid', 'Укажите, пожалуйста, адрес обслуживаемого дома')
        
        var grid = w2ui ['mgmt_contract_objects_grid']
        
        v.uuid_contract = $_REQUEST.id

        var data = $('body').data ('data')

        query ({type: 'contract_objects', action: 'create', id: undefined}, {data: v}, function () {
        
            if (data.item.id_customer_type == 1) {
            
                reload_page ()
            
            }
            else {

                w2popup.close ()

                grid.reload (grid.refresh)

            }
            
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))

        data.record = {
            uuid_contract_agreement: "",
            startdate: dt_dmy (data.item.effectivedate),
            enddate:   dt_dmy (data.item.plandatecomptetion),
        }
        
        query ({type: "contract_docs", id: undefined}, {search: [
        
            {field: "uuid_contract", operator: "is", value: $_REQUEST.id},
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