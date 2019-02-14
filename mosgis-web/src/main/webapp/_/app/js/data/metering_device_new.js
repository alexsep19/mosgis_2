define ([], function () {

    $_DO.update_metering_device_new = function (e) {
/*    
        var form = w2ui ['metering_device_new_form']

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
*/        
    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        var it = data.item

        data.record = {}

        query ({type: 'metering_devices', id: null, part: 'vocs'}, {}, function (d) {
        
            var types = d.vc_meter_types
                .filter (function (i) {return i.is_condo == it.is_condo})
                
            function go () {
                $.each (types, function () {this.text = this.label})
                data.types = types
                if (types.length) data.record.id_type = types [0].id
                done (data)            
            }
            
            if (!it.is_condo) return go ()
/*
            var classes = null; 
            
            $.each (types, function () {
                var c = this.clazz
                if (!c) return
                if (!classes) classes = {}
                classes [c] = 0
            })                        
*/            
            
            query ({type: 'premises', id: null}, {data: {uuid_house: $_REQUEST.id}}, function (dd) {
            
                var tmp = types.filter (function (i) {return i.clazz})
                
                types = types.filter (function (i) {return !i.clazz})
                
                $.each (tmp, function () {
                
                    var t = this
darn (t)                    
                    $.each (dd.vw_premises, function () {
                    
                        if (this.class != t.clazz) return
                        
                        types.push ({
                            id: t.id + '_' + this.id,
                            text: t.label + ' ' + this.label
                        })

                    })          
                
                }) 
            
/*            

                $.each (dd.vw_premises, function () {
                    var c = this.class
darn (c)                    
                    if (!c) return
                    if (!(c in classes)) return
                    classes [c] ++
                })          
                
                types = types
                    .filter (function (i) {
                        var c = i.clazz
                        return !c || classes [c]
                    })
                
darn (classes)                
*/

                go ()
            
            })                     

        })

    }

})