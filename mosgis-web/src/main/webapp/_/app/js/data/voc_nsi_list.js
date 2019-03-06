define ([], function () {   

    $_DO.open_nsi_list = function (e) {
        
        var id = e.target
        
        var type = 
            parseInt (id) > 0 ? 'voc_nsi' : 
//            /^rd_cols_\d+$/.test (id) ? 'voc_rd' : 
//            /^rd_voc_\d+$/.test (id) ? 'voc_rd_voc' : 
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

            if (org_vocs.length) data.vc_nsi_list_group.unshift ({
                name: '_ORG',
                label: 'Справочники организаций',
                nodes: org_vocs
            })

/*            
            data.vc_nsi_list_group.unshift ({
                name: '_RD',
                label: 'Справочники ГИС РД',
            })

            var rdx = {};

            $.each (data.vc_rd_list, function () { 
                var  id  = this.id
                this.id  = 'rd_cols_' + id
                rdx [id] = this 
            })
            
            $.each (data.vc_rd_list, function () { 
                if (!this.parent) return
                var p = rdx [this.parent]
                if (!p.nodes) p.nodes = []
                p.nodes.push (this)
            })

            data.vc_nsi_list.unshift ({
                listgroup: '_RD',
                id: 'rd_cols_1',
                text: 'Список атрибутов МКД в ГИС РД',
                nodes: rdx [1].nodes
            })
*/            
            
            
            
            
            
            
            
            
                                    
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
/*            
            $.each (data.vc_rd_cols, function () { 
                var  id  = this.id
                this.id  = 'rd_voc_' + id
                data.vc_nsi_list_group [0].nodes.push (this)
            })            
*/            
            $('body').data ('vc_nsi_list', vc_nsi_list)
            
            done (data)
            
        })        

    }

})