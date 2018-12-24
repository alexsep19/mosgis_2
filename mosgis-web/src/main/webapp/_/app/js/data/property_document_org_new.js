define ([], function () {

    var form_name = 'property_document_form'

    $_DO.open_orgs_property_document_org_new = function (e) {
    
        var f = w2ui [form_name]
        
        var saved = {
            data: clone ($('body').data ('data')),
            record: clone (f.record)
        }
        
        function done () {
            $('body').data ('data', saved.data)
            $_SESSION.set ('record', saved.record)
            use.block ('property_document_org_new')
        }
    
        $('body').data ('voc_organizations_popup.callback', function (r) {

            if (!r) return done ()
            
            saved.record.uuid_org_owner = r.uuid
            saved.record.label_org_owner = r.label
                
            done ()

        })

        use.block ('voc_organizations_popup')

    }

    $_DO.update_property_document_org_new = function (e) {

        var form = w2ui [form_name]

        var r = form.record
        var v = form.values ()
        
        if (!(v.uuid_org_owner = r.uuid_org_owner)) die ('label_org_owner', 'Вы забыли указать собственника')
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
        
        var r = $_SESSION.delete ('record')
                
/*        
        if (!('automaticrolloveroneyear' in r)) r.automaticrolloveroneyear = '0'        
        
        if (!r.label_org_owner) r.label_org_owner = 'Собственники объекта жилищного фонда'

        if (!r.vc_nsi_58) r.vc_nsi_58 = data.vc_nsi_58.items.filter (function (i) {return 'it.isdefault' in i})
        
        if (!r.code_vc_nsi_58) r.code_vc_nsi_58 = r.vc_nsi_58.filter (function (i) {return i ['it.isdefault'] == 1}) [0].id
*/        
        data.record = r || {prc: 100}
        
        if (!data.record.uuid_org_owner) return done (data)
        
        query ({type: 'premises', id: undefined}, {data: {uuid_house: $_REQUEST.id}}, function (d) {
        
            data.premises = d.vw_premises.map (function (i) {return {
                id: i.id, 
                text: i.label
            }})

            done (data)

        })

    }

})