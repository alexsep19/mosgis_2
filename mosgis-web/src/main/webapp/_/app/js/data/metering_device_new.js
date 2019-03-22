define ([], function () {

    $_DO.update_metering_device_new = function (e) {

        var form = w2ui ['metering_device_new_form']

        var v = form.values ()

        if (!v.id_type) die ('id_type', 'Укажите, пожалуйста, какой прибор требуется зарегистрировать')
        if (!v.mask_vc_nsi_2) die ('mask_vc_nsi_2', 'Укажите, пожалуйста, тип измеряемого ресурса')
        if (v.consumedvolume && v.mask_vc_nsi_2 == 4) die ('consumedvolume', 'Прибор учёта электроэнергии может поставлять только текущие показания, а не потреблённый объём')
        if (!v.meteringdevicestamp) die ('meteringdevicestamp', 'Укажите, пожалуйста, марку прибора')
        if (!v.meteringdevicemodel) die ('meteringdevicemodel', 'Укажите, пожалуйста, модель прибора')
        if (!v.meteringdevicenumber) die ('meteringdevicenumber', 'Укажите, пожалуйста, серийный (заводской) номер прибора')
        if (!v.factorysealdate) die ('factorysealdate', 'Укажите, пожалуйста, дата опломбирования прибора заводом-изготовителем')
        
        var t = String (v.id_type).split ('_')  
        
        v.id_type       = t [0]
        v.uuid_premise  = t [1] || null
        v.fiashouseguid = $('body').data ('data').item.fiashouseguid
        
        query ({type: 'metering_devices', id: null, action: 'create'}, {data: v}, function (data) {

            w2popup.close ()

            w2confirm ('Прибор учёта зарегистрирован. Открыть его страницу в новой вкладке?').yes (function () {openTab ('/metering_device/' + data.id)})

            var grid = w2ui ['house_metering_devices_grid']

            grid.reload (grid.refresh)

        })

    }
    
    function sample_by_premise (types, vw_premises) {
    
        var tmp = types.filter (function (i) {return i.clazz})
        
        var result = []

        $.each (tmp, function () {

            var t = this

            $.each (vw_premises, function () {

                if (this.class != t.clazz) return

                if (t.code_vc_nsi_30 && (t.code_vc_nsi_30 != this.code_vc_nsi_30)) return

                result.push ({
                    id: t.id + '_' + this.id,
                    label: t.label + ' №' + this.label
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
            
            data.vc_nsi_2.items = data.vc_nsi_2.items.filter (function (i) {return (i.id & mask) == i.id})

            if (!data.vc_nsi_2.items.length) die ('foo', 'Ваша организация не предоставляет ни одной услуги, связанной с коммунальными ресурсами, для которых предусмотрена установка приборов учёта. Проверьте, пожалуйста, полностью ли оформлен ваш договор управления.')
            
            data.record.mask_vc_nsi_2 = data.vc_nsi_2.items [0].id

            done (data)

        }) 

    }
    
    function load_ca_nsi2_and_show_popup (data, done) {

          query ({type: 'charter_object_services', id: null}, {limit:10000, offset:0, search: [{field: "uuid_charter_object", operator: "is", value: data.cach.uuid}]}, function (d) {
        
            var mask = 0
        
            $.each (d.root, function () {
                mask |= [1 << (this.code_vc_nsi_2 - 1)]
            })
            
            data.vc_nsi_2.items = data.vc_nsi_2.items.filter (function (i) {return (i.id & mask) == i.id})
            
            if (!data.vc_nsi_2.items.length) die ('foo', 'Ваша организация не предоставляет ни одной услуги, связанной с коммунальными ресурсами, для которых предусмотрена установка приборов учёта. Проверьте, пожалуйста, полностью ли оформлен ваш договор управления.')
            
            data.record.mask_vc_nsi_2 = data.vc_nsi_2.items [0].id

            done (data)

        }) 

    }
    
    function load_nsi2_by_srca_and_show_popup (data, done) {
    
        var srca = data.srca
        
        if (srca ['org.uuid'] != $_USER.uuid_org) srca = null
        
        if (!srca) die ('foo', 'Не найден объект договора для данного дома')
        
        query ({type: 'supply_resource_contract_object_subjects', id: null}, {limit: 10000, offset:0, data: {uuid_sr_ctr_obj: srca.uuid}}, function (d) {        

            var mask = 0
        
            $.each (d.tb_sr_ctr_subj, function () {
                mask |= [1 << (this.code_vc_nsi_2 - 1)]
            })
            
            data.vc_nsi_2.items = data.vc_nsi_2.items.filter (function (i) {return (i.id & mask) == i.id})
            
            if (!data.vc_nsi_2.items.length) die ('foo', 'Ваша организация не поставляет в этот дом ни одного ресурса, для которого предусмотрена установка приборов учёта. Проверьте, пожалуйста, полностью ли оформлен ваш договор ресурсоснабжения.')
            
            data.record.mask_vc_nsi_2 = data.vc_nsi_2.items [0].id

            done (data)
        
        })

    }
    
    function load_nsi2_by_cach_and_show_popup (data, done) {

        var cach = data.cach
        
        if (cach ['org.uuid'] != $_USER.uuid_org) cach = null
        
        if (!cach) die ('foo', 'Не найден объект управления для данного дома')
        
        if (cach ['ctr.uuid']) return load_ctr_nsi2_and_show_popup (data, done)

        if (cach ['ca.uuid']) return load_ca_nsi2_and_show_popup (data, done)
        
        if (!cach) die ('foo', 'Не опознан объект управления для данного дома')
                            
    }

    function load_nsi2_and_show_popup (data, done) {

        $.each (data.types, function () {this.text = this.label})
                
        if (data.types.length) data.record.id_type = data.types [0].id
        
        if ($_USER.role.nsi_20_2) {
        
            load_nsi2_by_srca_and_show_popup (data, done)
            
        }
        else {
        
            load_nsi2_by_cach_and_show_popup (data, done)
            
        }        

    }

    return function (done) {

        var data = clone ($('body').data ('data'))
        
        var it = data.item

        data.record = {consumedvolume: 0}

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