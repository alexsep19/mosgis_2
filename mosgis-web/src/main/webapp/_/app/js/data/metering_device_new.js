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
    
    function sample_by_premise (types, vw_premises) {
    
        var tmp = types.filter (function (i) {return i.clazz})
        
        var result = []

        $.each (tmp, function () {

            var t = this

            $.each (vw_premises, function () {

                if (this.class != t.clazz) return

                if (t.code_vc_nsi_30 != this.code_vc_nsi_30) return

                if (t.code_vc_nsi_30)

                result.push ({
                    id: t.id + '_' + this.id,
                    label: t.label + ' ' + this.label
                })

            })          

        }) 
                
        return result
    
    }
    
    function load_ctr_nsi2_and_show_popup (data, done) {    
    
        query ({type: 'contract_object_services', id: null}, {limit:10000, offset:0, search: [{field: "uuid_contract_object", operator: "is", value: data.cach.uuid}]}, function (d) {
        
            var mask = 0
        
            $.each (d.root, function () {
                mask |= [1 << (this.code_vc_nsi_2 - 1)]
            })
            
            data.vc_nsi_2.items = data.vc_nsi_2.items.filter (function (i) {return i.id & mask})

            done (data)

        }) 

    }
    
    function load_ca_nsi2_and_show_popup (data, done) {

        done (data)

    }
    
    function load_nsi2_and_show_popup (data, done) {

        $.each (data.types, function () {this.text = this.label})
                
        if (data.types.length) data.record.id_type = data.types [0].id
        
        var cach = data.cach
        
        if (!cach) die ('foo', 'Не найден объект управления для данного дома')
        
        if (cach ['ctr.uuid']) return load_ctr_nsi2_and_show_popup (data, done)
        
        if (cach ['ca.uuid']) return load_ca_nsi2_and_show_popup (data, done)
        
        if (!cach) die ('foo', 'Не опознан объект управления для данного дома')
                            
    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        var it = data.item

        data.record = {}

        query ({type: 'metering_devices', id: null, part: 'vocs'}, {}, function (d) {
        
            data.types = d.vc_meter_types.filter (function (i) {return i.is_condo == it.is_condo})
                
            add_vocabularies (d, d); for (k in d) data [k] = d [k]
                            
            if (!it.is_condo) return load_nsi2_and_show_popup (data, done)
            
            query ({type: 'premises', id: null}, {data: {uuid_house: $_REQUEST.id}}, function (dd) {
                            
                data.types = data.types.filter (function (i) {return !i.clazz}).concat (sample_by_premise (data.types, dd.vw_premises))
                            
                load_nsi2_and_show_popup (data, done)
            
            })                     

        })

    }

})