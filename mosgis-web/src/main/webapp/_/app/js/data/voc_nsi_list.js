define ([], function () {   

    $_DO.open_nsi_list = function (e) {
        
        var id = e.target
        
        var type = 
            parseInt (id) > 0 ? 'voc_nsi' : 
            id

        if (!type || /[A-Z]$/.test (type)) return
                
        w2utils.lock ($(w2ui ['vocs_layout'].el ('main')), {spinner: true})

        use.block (type)

    }

    function one_of_roles () {

        if ($_USER.role.admin) return true

        for (var i = 0; i < arguments.length; i++) if ($_USER.role ['nsi_20_' + arguments [i]]) return true

        return false

    }

    return function (done) {

        query ({type: 'voc_nsi_list'}, {}, function (data) {        
        
            var org_vocs = []
                        
            if (one_of_roles (1, 19, 20, 21, 22, 36, 2)) org_vocs.push ({
                id: 'add_services',
                text: 'Дополнительные услуги',
            })
        
            if (one_of_roles (1, 19, 20, 21, 36, 2)) org_vocs.push ({
                id: 'municipal_services',
                text: 'Коммунальные услуги',
            })

            if (one_of_roles (1, 19, 20, 21, 22)) org_vocs.push ({
                id: 'org_works',
                text: 'Работы и услуги организаций',
            })

            if (one_of_roles (1, 19, 20, 21, 36)) org_vocs.push ({
                id: 'insurance_products',
                text: 'Страховые продукты',
            })
            
            if (one_of_roles (1, 19, 20, 21, 22, 36, 2)) org_vocs.push ({
                id: 'vc_persons',
                text: 'Физические лица',
            })

            if (one_of_roles (7)) org_vocs.push ({
                id: 'vc_oh_wk_types',
                text: 'Вид работ капитального ремонта',
            })
            
            if (one_of_roles (1, 19, 20, 21, 22, 36, 2)) org_vocs.push ({
                id: 'general_needs_municipal_resources',
                text: '337. Потребляемые ком. ресурсы',
            })

            if (org_vocs.length) data.vc_nsi_list_group.unshift ({
                name: '_ORG',
                label: 'Справочники организаций',
                nodes: org_vocs
            })
            
            var tariff_vocs = []            
            if (one_of_roles (8, 10)) tariff_vocs.push ({
                id: 'voc_differentiation',
                text: 'Критерии дифференциации',
            })            
            if (tariff_vocs.length) data.vc_nsi_list_group.push ({
                name: '_TARIFF',
                label: 'Тарифы',
                nodes: tariff_vocs
            })                        

            var msp_vocs = []
            if (one_of_roles (9, 10)) msp_vocs.push ({
                id: 'msp_decision_bases',
                text: 'Основание принятия решения о мерах социальной поддержки гражданина',
            })            
            if (tariff_vocs.length) data.vc_nsi_list_group.push ({
                name: '_MSP',
                label: 'Социальная поддержка',
                nodes: msp_vocs
            })

            var idx = {}; 

            $.each (data.vc_nsi_list_group, function () {
                
                this.img   = 'icon-folder'
                this.id    = this.name
                this.text  = this.label
                if (!this.nodes) this.nodes = []
                
                idx [this.name] = this
                
            })
            
            var vc_nsi_list = {}
            
            $.each (data.vc_nsi_list, function () {
                vc_nsi_list [this.id] = this.text
                idx [this.listgroup].nodes.push (this)
            })

            $('body').data ('vc_nsi_list', vc_nsi_list)
            
            done (data)
            
        })        

    }

})