define ([], function () {

    $_DO.update_charter_object_common_service_popup = function (e) {

        var form = w2ui ['voc_user_form']

        var v = form.values ()
        
        if (!v.enddate) die ('enddate', 'Укажите, пожалуйста дату окончания управления')
        if (v.enddate < v.startdate) die ('enddate', 'Дата начала превышает дату окончания управления')

        var grid = w2ui ['charter_object_common_services_grid']
        
        v.uuid_charter_object = $_REQUEST.id

        query ({type: 'charter_object_services', action: 'update', id: form.record.uuid}, {data: v}, function () {

            w2popup.close ()

            grid.reload (grid.refresh)

        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        var grid = w2ui ['charter_object_common_services_grid']

        var r = grid.get (grid.getSelection () [0])
        r.startdate = dt_dmy (r.startdate)
        r.enddate = dt_dmy (r.enddate)
        
        data.record = r

        query ({type: "charter_docs", id: undefined}, {search: [
        
            {field: "uuid_charter", operator: "is", value: data.item.uuid_charter},
            {field: "uuid_charter_object", operator: "is", value: data.item.uuid},
            
        ]}, function (d) {
        
            var a = [{id: "", text: "Текущий устав"}]
            
            $.each (d.tb_charter_files, function () {
                a.push ({id: this.id, text: "Протокол собрания " + this.label})
            })
            
            data.agreements = a
            
            done (data)

        })                      

    }

})