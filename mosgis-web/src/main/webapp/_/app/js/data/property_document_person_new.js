define ([], function () {

    var form_name = 'property_document_form'

    $_DO.update_property_document_person_new = function (e) {

        var form = w2ui [form_name]

        var v = form.values ()
        
        if (!v.uuid_person_owner) die ('uuid_person_owner', 'Вы забыли указать собственника')
        if (!v.uuid_premise) die ('uuid_premise', 'Вы забыли указать помещение')
     
        var p = parseFloat (v.prc)
        if (!(p > 0 && p <= 100)) die ('prc', 'Некорректно указан размер доли')
        
        if (v.dt && v.dt > new Date ().toISOString ()) die ('dt', 'Дата документа не может находиться в будущем')
                
        query ({type: 'property_documents', id: undefined, action: 'create'}, {data: v}, function (data) {
        
            w2popup.close ()
            
            if (data.id) w2confirm ('Документ о праве собственности зарегистрирован. Открыть его страницу в новой вкладке?').yes (function () {openTab ('/property_document/' + data.id)})
            
            var grid = w2ui ['house_property_documents_grid']
            
            grid.reload (grid.refresh)            
        
        })

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
                           
        data.record = {prc: 100}
                
        query ({type: 'premises', id: undefined}, {data: {uuid_house: $_REQUEST.id}}, function (d) {
        
            data.premises = d.vw_premises.map (function (i) {return {
                id: i.id, 
                text: i.label
            }})
            
            query ({type: 'vc_persons', id: undefined}, {offset: 0, limit: 1000000}, function (d) {

                data.vc_persons = d.root.map (function (i) {return {
                    id: i.id, 
                    text: i.label
                }})

            })

            done (data)

        })

    }

})