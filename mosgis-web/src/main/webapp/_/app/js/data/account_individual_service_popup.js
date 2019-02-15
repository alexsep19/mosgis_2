define ([], function () {

    $_DO.update_account_individual_service_popup = function (e) {
    
        var form = w2ui ['account_individual_service_popup_form']

        var v = form.values ()
        
        if (!v.uuid_add_service)     die ('uuid_add_service', 'Укажите, пожалуйста, услугу из справочника')       
        
        if (!v.begindate) die ('begindate', 'Укажите, пожалуйста, дату начала')
        if (!v.enddate) die ('enddate', 'Укажите, пожалуйста, дату окончания')        

        if (v.enddate < v.begindate) die ('enddate', 'Дата начала управления превышает дату окончания')
        
        function done () {
            w2popup.close ()
            var grid = w2ui ['account_common_individual_services_grid']
            grid.reload (grid.refresh)
        }
        
        var id = form.record.uuid

        if (id && !v.files) {
        
            query ({type: 'account_individual_services', id: id, action: 'edit'}, {data: v}, done)
            
        }
        else {
        
            var file = get_valid_gis_file (v, 'files')
            
            var data = {
                uuid_account: $_REQUEST.id,
                uuid_add_service: v.uuid_add_service,
                begindate: v.begindate,
                enddate: v.enddate,
            }
            
            if (id) data.uuid = id

            Base64file.upload (file, {
                type: 'account_individual_services',
                data: data,
                onprogress: show_popup_progress (file.size),
                onloadend: done
            })

        }
        
    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        var it = data.item

        if (it ['ca.uuid']) {
            data.dt_from = it ['ca.effectivedate']
            data.dt_to   = it ['ca.plandatecomptetion']
        }

        if (it ['ch.uuid']) {
            data.dt_from = it ['ca.date_']
            data.dt_to   = it ['ca.terminate'] || new Date ().toJSON ()
        }
        
        data.dt_from = dt_dmy (data.dt_from)
        data.dt_to   = dt_dmy (data.dt_to)

        data.record = $_SESSION.delete ('record') || {
            begindate: data.dt_from,
            enddate:   data.dt_to,
        }
        
        query ({type: 'add_services', id: undefined}, {limit: 100000, offset: 0}, function (d) {
        
            data.add_services = d.root.map (function (r) {return {
                id: r.uuid,
                text: r.label
            }})

            done (data)

        })

    }

})