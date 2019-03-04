define ([], function () {

    var grid_name = 'metering_device_common_metering_values_grid'

    function recalcToolbar (e) {e.done (function () {
    
        var g = w2ui [grid_name]

        var t = g.toolbar
        var r = g.get (g.getSelection () [0])

        var id_status = r ? r.id_ctr_status : -1

        if (r && r.id_type != 3 && r.id_ctr_status == 10) {
            t.enable ('approve')
        }
        else {
            t.disable ('approve')
        }
        
        switch (id_status) {        
            case 10:
            case 14:
            case 34:
                t.enable ('delete')
                break
            default:
                t.disable ('delete')
        }        
        
        switch (id_status) {        
            case 10:
                t.enable ('edit')
                break
            default:
                t.disable ('edit')
        }        
        
    })}

    return function (data, view) {
    
        var it = data.item       
        
        var buttons = []

        if (it._can.edit_values) {
        
            buttons = data.resources.items.map (function (i) {
                return {
                    type: 'button', 
                    id: 'resource_' + i.id,
                    caption: i.text, 
                    icon: 'w2ui-icon-plus', 
                    onClick: $_DO.create_metering_device_common_metering_values, 
                }
            })           

            buttons.push ({type: 'button', id: 'edit', caption: 'Изменить', onClick: $_DO.edit_metering_device_common_metering_values, disabled: true, icon: 'w2ui-icon-pencil'})
            buttons.push ({type: 'button', id: 'delete', caption: 'Удалить', onClick: $_DO.delete_metering_device_common_metering_values, disabled: true, icon: 'w2ui-icon-cross'})
            buttons.push ({type: 'button', id: 'approve', caption: 'Разместить', onClick: $_DO.approve_metering_device_common_metering_values, disabled: true, off: $_USER.role.admin || it.id_ctr_status != 40})

        }

        $(w2ui ['passport_layout'].el ('main')).w2regrid ({ 

            name: grid_name,
            
            multiSelect: false,
            
            show: {
                toolbar: true,
                toolbarInput: false,
                footer: true,
            },     
            
            toolbar: {items: buttons},              

            columns: [                

                {field: 'datevalue', caption: 'Дата', size: 18, render: _dt},

                {field: 'id_type', caption: 'Тип', size: 18, voc: data.vc_meter_value_types},
                {field: 'code_vc_nsi_2', caption: 'Ресурс', size: 18, off: it.mask_vc_nsi_2 < 17, voc: data.resources},

                {field: 'meteringvaluet1', caption: it.tariffcount > 1 ? 'Показание T1' : 'Показание', size: 50, render: 'float:7'},
                {field: 'meteringvaluet2', caption: 'Показание T2', size: 50, render: 'float:7', off: it.tariffcount < 2},
                {field: 'meteringvaluet3', caption: 'Показание T3', size: 50, render: 'float:7', off: it.tariffcount < 3},

                {field: 'id_ctr_status', caption: 'Статус', size: 100, voc: data.vc_gis_status},
                {field: 'out_soap.ts', caption: 'Отправлено',    size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'out_soap.ts_rp', caption: 'Обработано',    size: 30, render: _ts, attr: 'data-ref=1'},
                {field: 'out_soap.err_text', caption: 'Ошибка',    size: 30},

            ].filter (not_off),

            postData: {data: {uuid_meter: $_REQUEST.id}},

            url: '/mosgis/_rest/?type=metering_device_values',

            onSelect: recalcToolbar,
            onUnselect: recalcToolbar,
            
            onClick: function (e) {                
            
                var r = this.get (e.recid)
            
                switch (this.columns [e.column].field) {
                    case 'out_soap.ts':    if (r ['out_soap.ts']) return openTab ('/out_soap_rq/' + r.id_log)
                    case 'out_soap.ts_rp': if (r ['out_soap.ts_rp']) return openTab ('/out_soap_rp/' + r ['out_soap.uuid_ack'])
                }                

            }            

        }).refresh ();

    }

})